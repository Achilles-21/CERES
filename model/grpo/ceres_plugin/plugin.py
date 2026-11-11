# -*- coding: utf-8 -*-


import hashlib
import json
import logging
import math
import os
import sys


_HERE = os.path.dirname(os.path.abspath(__file__))
if _HERE not in sys.path:
    sys.path.insert(0, _HERE)

from grammar import check_grammar_global, check_grammar_turn, parse_actions  # noqa: E402
from store import TrajectoryStore, anchor_key, final_eval, make_traj_key  # noqa: E402
from student_sim import apply_delta, init_state, student_chat  # noqa: E402
from rewards import group_advantages, load_reward_config, sequence_components  # noqa: E402

__all__ = [
    "teacher_env", "ceres_sequence_reward", "ceres_anchor_reward",
    "CeresSequenceORM", "CeresAnchorORM", "W", "RISK_PATTERNS", "STORE",
    "SWIFT_AVAILABLE", "orms", "multi_turns", "DEFAULT_END_PENALTY",
]

_LOG = logging.getLogger(__name__)


DEFAULT_MAX_TURNS = 5
DEFAULT_NUM_GENERATIONS = 8


DEFAULT_END_PENALTY = 0.0


def _env_int(name, default):
    try:
        return int(float(str(os.environ.get(name, default)).strip()))
    except Exception:
        return default


def _env_float(name, default):
    try:
        v = float(str(os.environ.get(name, default)).strip())
        return v if math.isfinite(v) else default
    except Exception:
        return default


def _end_penalty():
    return max(0.0, _env_float("CERES_END_PENALTY", DEFAULT_END_PENALTY))


def _max_turns():
    return max(1, _env_int("CERES_MAX_TURNS", DEFAULT_MAX_TURNS))


def _num_generations():
    return max(1, _env_int("CERES_NUM_GENERATIONS", DEFAULT_NUM_GENERATIONS))


STORE = TrajectoryStore()


multi_turns = {}

try:
    from swift.plugin import ORM, orms  # noqa: E402
    SWIFT_AVAILABLE = True
except Exception as _e:
    SWIFT_AVAILABLE = False

    class _StubORM:
        def __call__(self, **kwargs):
            raise NotImplementedError

    ORM = _StubORM
    orms, multi_turns = {}, {}
    _LOG.warning(
        "[ceres plugin] swift 不可用（%s）。已降级为纯逻辑模式：teacher_env 与奖励函数仍可"
        "独立运行，但 orms/multi_turns 只注册到本地空表；真实训练必须在装有 ms-swift "
        "3.2.1 的环境用 --external_plugins ceres_plugin/action_plugin.py 或 scalar_plugin.py 加载。", _e)


def _msg_text(content):
    if isinstance(content, str):
        return content
    if isinstance(content, (list, tuple)):
        parts = []
        for item in content:
            if isinstance(item, str):
                parts.append(item)
            elif isinstance(item, dict) and isinstance(item.get("text"), str):
                parts.append(item["text"])
        return "\n".join(parts)
    if isinstance(content, dict):
        for key in ("text", "content"):
            val = content.get(key)
            if isinstance(val, str):
                return val
        return ""
    return "" if content is None else str(content)


def _s(v):
    if v is None:
        return ""
    if isinstance(v, str):
        return v
    if isinstance(v, (list, tuple, dict)):
        try:
            return json.dumps(v, ensure_ascii=False, default=str)
        except Exception:
            return str(v)
    return str(v)


def _uid_of(_input):
    try:
        v = _input.get("uid", "")
        return "" if v is None else str(v)
    except Exception:
        return ""


def _count_assistant(messages):
    n = 0
    for m in messages if isinstance(messages, (list, tuple)) else []:
        if isinstance(m, dict) and m.get("role") == "assistant":
            n += 1
    return n


def _last_teacher_text(messages):
    if not isinstance(messages, (list, tuple)):
        return ""
    for m in reversed(messages):
        if isinstance(m, dict) and m.get("role") == "assistant":
            return _msg_text(m.get("content"))
    return ""


def _persona_seed_int(dd):
    v = dd.get("persona_seed", 0)
    try:
        return int(v)
    except (TypeError, ValueError):
        pass
    try:
        return int(float(v))
    except (TypeError, ValueError):
        pass
    s = "" if v is None else str(v)
    if not s.strip():
        return 0
    return int(hashlib.sha1(s.encode("utf-8", "ignore")).hexdigest()[:8], 16)


def _mean(xs):
    xs = list(xs)
    return sum(xs) / len(xs) if xs else 0.0


def _std0(xs):
    'Use population standard deviation for reward normalization.'
    xs = list(xs)
    if not xs:
        return 0.0
    m = _mean(xs)
    return math.sqrt(sum((x - m) ** 2 for x in xs) / len(xs))


def _clip(x, lo, hi):
    return lo if x < lo else (hi if x > hi else x)


def _to_float(v, default=0.0):
    try:
        f = float(v)
    except (TypeError, ValueError):
        return default
    return f if math.isfinite(f) else default


def _end_reason(teacher_text, n_assistant, finish_reason):
    'Stop on an end marker, turn limit, or generation-length limit, in that order.'


    if "<end" in (teacher_text or ""):
        return "end_tag"
    if n_assistant >= _max_turns():
        return "max_turns"
    if finish_reason == "length":
        return "length"
    return None


def _lang_of(dd):
    dd = dd if isinstance(dd, dict) else {}
    lang = dd.get("lang")
    return "en" if isinstance(lang, str) and lang.strip().lower() == "en" else "zh"


def _student_messages(dd, state, teacher_text):
    try:
        state_json = json.dumps(state, ensure_ascii=False, default=str)
    except Exception:
        state_json = str(state)
    if _lang_of(dd) == "en":
        return [{
            "role": "user",
            "content": (
                f"[Course context / findings] {_s(dd.get('courseware_context', ''))}\n"
                f"[Learner profile] {_s(dd.get('learner_profile', ''))}\n"
                f"[Current learner state] {state_json}\n"
                f"[Teacher's action this turn]\n{teacher_text}\n"
                f"Please respond as an ophthalmology resident-in-training and output JSON."
            )}]
    return [{
        "role": "user",
        "content": (
            f"【课程上下文/检查所见】{_s(dd.get('courseware_context', ''))}\n"
            f"【学习者画像】{_s(dd.get('learner_profile', ''))}\n"
            f"【当前学习者状态】{state_json}\n"
            f"【教师本轮动作】\n{teacher_text}\n"
            f"请以眼科规培生身份回应，并输出 JSON。"
        )}]


def _append_step(rec, turn, teacher_text, student_reply, state_before, state_after,
                 delta, degraded, dd, fixed_step_reward=None):
    parsed = [tag for tag, _a, _b in parse_actions(teacher_text)]
    teaching = [tag for tag in parsed if tag != "end"]
    invalid = os.environ.get("CERES_CREDIT_MODE") == "action" and (
        not check_grammar_turn(teacher_text) or len(teaching) > 1 or
        (not teaching and "end" not in parsed))
    rec["steps"].append({
        "turn": turn,
        "teacher": teacher_text,
        "student_reply": student_reply,
        "state_before": dict(state_before),
        "state_after": dict(state_after),
        "reported_delta": dict(delta),
        "realized_delta": {k: state_after.get(k, 0.0) - state_before.get(k, 0.0)
                           for k in ("mastery", "engagement", "fatigue")},
        "step_reward": fixed_step_reward if fixed_step_reward is not None else 0.0,
        "anchor": anchor_key(dd, state_before, teacher_text),
        "anchor_version": "pre-state-v1",
        "action_type": "invalid" if invalid else next(iter(teaching), "end"),
        "invalid_bundle": invalid,
        "turn_ok": check_grammar_turn(teacher_text),
        "misconception_resolved": bool(delta.get("misconception_resolved")),
        "degraded": bool(degraded),
    })


def _resolved_seen(steps):
    for st in steps:
        if not isinstance(st, dict):
            continue
        if st.get("misconception_resolved"):
            return True
        sb = st.get("state_before") if isinstance(st.get("state_before"), dict) else {}
        sa = st.get("state_after") if isinstance(st.get("state_after"), dict) else {}
        if _to_float(sb.get("misconception_severity"), None) is not None:
            drop = (_to_float(sb.get("misconception_severity"))
                    - _to_float(sa.get("misconception_severity"), 0.0))
            if drop >= 0.3:
                return True
    return False


def _g4_state_violation(steps):
    'A displayed misconception requires correction or evidence of resolution before ending.'


    if not steps:
        return False
    last = steps[-1] if isinstance(steps[-1], dict) else {}
    last_acts = [t for t, _a, _b in parse_actions(last.get("teacher", ""))]
    if "end" not in last_acts:
        return False
    for st in steps:
        if not isinstance(st, dict):
            continue
        for t, _a, _b in parse_actions(st.get("teacher", "")):
            if t == "correct":
                return False
    return not _resolved_seen(steps)


def _seal(_input, rec, state, teacher_text, reason, turn):
    'Seal once per turn; identical teacher text can still represent distinct actions.'


    steps = rec.setdefault("steps", [])
    if not isinstance(steps, list):
        steps = rec["steps"] = []
    last_step = steps[-1] if steps else None


    if not (isinstance(last_step, dict) and last_step.get("turn") == turn):
        _append_step(rec, turn, teacher_text, "", dict(state), dict(state), {}, False,
                     _input, fixed_step_reward=0.0)

    uid = str(rec.get("uid", "") or _uid_of(_input))
    rec["uid"] = uid
    rec["lang"] = _lang_of(_input)
    rec["final_state"] = final_eval(rec)
    global_ok = bool(check_grammar_global(rec["steps"], _input))
    if global_ok and _g4_state_violation(rec["steps"]):
        global_ok = False
    rec["grammar_global_ok"] = global_ok
    key = make_traj_key(uid, teacher_text)
    rec["traj_key"] = key
    rec["finished_reason"] = reason
    rec["context"] = {k: _input.get(k) for k in ("curriculum_node", "difficulty")}
    rec["source_uid"] = _input.get("source_uid", uid)
    rec["state_grammar_violation"] = _g4_state_violation(rec["steps"])
    cfg = rec.setdefault("reward_config", load_reward_config())
    rec["reward_components"] = sequence_components(rec, cfg, RISK_PATTERNS)


    # Allocate terminal reward to a teaching action, not a control-only ending.
    owner = max((i for i, s in enumerate(rec["steps"]) if s["action_type"] != "end"), default=0)
    for i, step in enumerate(rec["steps"]):
        step["step_reward"] = rec["reward_components"]["total"] if i == owner else 0.0
        step["credit_step"] = owner if step["action_type"] == "end" else i
        step["step_reward_source"] = cfg["step_reward_source"]
    STORE.put(key, rec)
    STORE.flush(rec)
    _input["_ceres_traj"] = rec
    _input["_ceres_state"] = state


def _step_one(_input):
    if _input.get("finished"):
        return

    messages = _input.get("messages")
    if not isinstance(messages, list):
        messages = _input["messages"] = []
    teacher_text = _last_teacher_text(messages)
    n_assistant = _count_assistant(messages)

    state = _input.get("_ceres_state")
    if not (isinstance(state, dict) and state):
        state = init_state(_input)
    rec = _input.get("_ceres_traj")
    if not (isinstance(rec, dict) and isinstance(rec.get("steps"), list)):
        rec = {"uid": _uid_of(_input), "steps": [], "reward_config": load_reward_config()}
    _input["_ceres_state"] = state
    _input["_ceres_traj"] = rec

    reason = _end_reason(teacher_text, n_assistant, _input.get("finish_reason"))
    action_mode = os.environ.get("CERES_CREDIT_MODE", "scalar") == "action"
    actions = [tag for tag, _a, _b in parse_actions(teacher_text) if tag != "end"]


    if reason and (not action_mode or not actions):
        _seal(_input, rec, state, teacher_text, reason, max(0, n_assistant - 1))
        _input["finished"] = True
        return

    turn = max(0, n_assistant - 1)
    state["turn"] = turn
    seed = _persona_seed_int(_input) + turn
    obs = _student_messages(_input, state, teacher_text)
    lang = _lang_of(_input)
    invalid_bundle = action_mode and (len(actions) != 1 or not check_grammar_turn(teacher_text))
    if invalid_bundle:
        rec.setdefault("rollout_defects", []).append("expected one pedagogical action per teacher turn")
        stu = {"reply": "Please issue one pedagogical action at a time.", "state_delta": {}}
    elif lang == "en":
        stu = student_chat(obs, seed=seed, lang="en")
    else:
        stu = student_chat(obs, seed=seed)
    if not isinstance(stu, dict):
        stu = {}

    raw_delta = stu.get("state_delta")
    delta = dict(raw_delta) if isinstance(raw_delta, dict) else {}
    delta.setdefault("misconception_shown", stu.get("misconception_shown", ""))

    state_before = dict(state)
    state_after = apply_delta(state, delta)


    reply = stu.get("reply")
    reply = reply if isinstance(reply, str) and reply.strip() else "……"
    _append_step(rec, turn, teacher_text, reply, state_before, dict(state_after),
                 delta, stu.get("degraded", False), _input)

    _input["_ceres_state"] = state_after
    _input["_ceres_traj"] = rec
    if reason:
        _seal(_input, rec, state_after, teacher_text, reason, turn)
        _input["finished"] = True
        return

    messages.append({"role": "user", "content": reply})
    _input["finished"] = False


def _maybe_clear_store_for_new_batch(inputs):
    'Clear only at the next batch so current records survive reward lookup.'


    try:
        dicts = [x for x in inputs if isinstance(x, dict)]
        if not dicts or any("_ceres_state" in x for x in dicts):
            return False
        STORE.clear()
        return True
    except Exception:
        return False


def teacher_env(inputs):
    'Preserve row identities and order; append a student reply or seal the finished trajectory.'


    if not isinstance(inputs, list):
        _LOG.warning("[ceres plugin] teacher_env 收到非 list 输入（%s），原样返回",
                     type(inputs).__name__)
        return inputs
    _maybe_clear_store_for_new_batch(inputs)
    for _input in inputs:
        if not isinstance(_input, dict):
            _LOG.warning("[ceres plugin] teacher_env 跳过非 dict 元素: %r", type(_input))
            continue
        try:
            _step_one(_input)
        except Exception as e:
            _LOG.warning("[ceres plugin] teacher_env 处理单条输入异常（保守结束）: %r", e)
            try:
                msgs = _input.get("messages")
                state = _input.get("_ceres_state")
                if not (isinstance(state, dict) and state):
                    state = init_state(_input)
                rec = _input.get("_ceres_traj")
                if not (isinstance(rec, dict) and isinstance(rec.get("steps"), list)):
                    rec = {"uid": _uid_of(_input), "steps": []}
                _seal(_input, rec, state, _last_teacher_text(msgs), "error",
                      max(0, _count_assistant(msgs) - 1))
            except Exception as e2:
                _LOG.warning("[ceres plugin] 保守封存也失败（放弃该条轨迹）: %r", e2)
            _input["finished"] = True
    return inputs


_cfg = load_reward_config()
W = dict(fmt=_cfg['alpha'], rule=_cfg['beta'], out=_cfg['gamma'], risk=_cfg['risk_weight'])

RISK_PATTERNS = [
    "不用查眼压", "滴眼液没有禁忌", "直接手术", "立刻手术", "这个剂量是",
    "不用散瞳", "激素随便用", "肯定不是青光眼", "确诊就是",
]


def _completion_text(c):
    if isinstance(c, str):
        return c
    if isinstance(c, (list, tuple)):
        return "\n".join(_msg_text(m.get("content", "")) for m in c if isinstance(m, dict))
    return "" if c is None else str(c)


def _uid_at(kwargs, i, n):
    col = kwargs.get("uid")
    if isinstance(col, (list, tuple)):
        v = col[i] if 0 <= i < len(col) else None
        return "" if v is None else str(v)
    if col is None:
        return ""
    return str(col) if n == 1 else ""


_SUFFIX_MATCH_MIN_LEN = 32


def _pick_by_messages(cands, kwargs, i):
    'Disambiguate matching trajectory keys using the full teacher-message history.'


    msgs = None
    col = kwargs.get("messages")
    if isinstance(col, (list, tuple)) and 0 <= i < len(col):
        msgs = col[i]
    texts = []
    if isinstance(msgs, (list, tuple)):
        texts = [_msg_text(m.get("content", "")) for m in msgs
                 if isinstance(m, dict) and m.get("role") == "assistant"]
    if texts:
        for cand in cands:
            if not isinstance(cand, dict):
                continue
            steps = cand.get("steps")
            steps = [s for s in steps if isinstance(s, dict)] if isinstance(steps, list) else []
            teachers = [s.get("teacher") if isinstance(s.get("teacher"), str) else ""
                        for s in steps]
            if teachers == texts:
                return cand
    _LOG.debug("[ceres plugin] traj_key 碰撞（%d 候选）且 kwargs['messages'] 不可判别 → "
               "取最后封存者", len(cands))
    return cands[-1] if cands else None


def _lookup_rec(kwargs, i, completions):
    'Match final text and message history; do not choose an ambiguous trajectory.'


    n = len(completions) if isinstance(completions, (list, tuple)) else 1
    uid = _uid_at(kwargs, i, n)
    text = _completion_text(completions[i]).strip()
    cands = STORE.get_all(make_traj_key(uid, text))
    rec = None
    if len(cands) == 1:
        rec = cands[0]
    elif len(cands) > 1:
        rec = _pick_by_messages(cands, kwargs, i)
    if rec is None and uid:
        allow_suffix = len(text) >= _SUFFIX_MATCH_MIN_LEN
        for cand in STORE.get_by_uid(uid):
            last_t = ""
            if isinstance(cand, dict):
                steps = cand.get("steps")
                if isinstance(steps, list) and steps and isinstance(steps[-1], dict):
                    last_t = _msg_text(steps[-1].get("teacher", "")).strip()
            if not (last_t and text):
                continue
            if last_t == text or (allow_suffix and (last_t.endswith(text) or text.endswith(last_t))):
                rec = cand
                break
    if rec is None:
        _LOG.warning("[ceres plugin] 轨迹检索失败：uid=%s… i=%d/%d → 奖励记 0.0",
                     (uid[:8] or "<空>"), i, n)
    return rec, uid


def ceres_sequence_reward(completions, **kwargs):
    'Return the sequence reward consumed once by the action trainer.'


    completions = list(completions or [])
    scores = []
    for i in range(len(completions)):
        rec, uid = _lookup_rec(kwargs, i, completions)
        if not isinstance(rec, dict):
            if os.environ.get("CERES_CREDIT_MODE") == "action":
                raise RuntimeError(f"Missing CERES trajectory: {uid}")
            scores.append(0.0)
            continue
        components = rec.get("reward_components")
        if components is None:
            components = sequence_components(rec, load_reward_config(), RISK_PATTERNS)
        scores.append(float(components["total"]))
    return scores


def ceres_anchor_reward(completions, **kwargs):
    'Scalar ablation only: averaging action advantages does not implement action-level credit.'


    if os.environ.get("CERES_CREDIT_MODE") == "action":
        raise RuntimeError("ceres_anchor ORM scalarizes credit; use the registered action trainer")
    completions = list(completions or [])
    recs, uids = [], []
    for i in range(len(completions)):
        rec, uid = _lookup_rec(kwargs, i, completions)
        recs.append(rec)
        uids.append(uid)
    credit = [0.0] * len(recs)
    by_uid = {}
    for i, uid in enumerate(uids):
        if uid and isinstance(recs[i], dict):
            by_uid.setdefault(uid, []).append(i)
    cfg = load_reward_config()
    for indices in by_uid.values():
        records = [recs[i] for i in indices]
        for rec in records:
            if "reward_components" not in rec:
                rec["reward_components"] = sequence_components(rec, cfg, RISK_PATTERNS)
        result = group_advantages(records, cfg)
        for i, row in zip(indices, result["action"]):
            credit[i] = sum(row) / len(row) if row else 0.0
    return credit


class CeresSequenceORM(ORM):
    def __call__(self, completions=None, **kwargs):
        return ceres_sequence_reward(completions or [], **kwargs)


class CeresAnchorORM(ORM):
    def __call__(self, completions=None, **kwargs):
        return ceres_anchor_reward(completions or [], **kwargs)


orms["ceres_sequence"] = CeresSequenceORM
orms["ceres_anchor"] = CeresAnchorORM
multi_turns["teacher_env"] = teacher_env


if SWIFT_AVAILABLE:
    try:
        from save_on_exit import register_save_on_exit  # noqa: E402
        _SAVE_ON_EXIT_REGISTERED = register_save_on_exit()
    except Exception as _save_exit_e:  # noqa: BLE001
        _SAVE_ON_EXIT_REGISTERED = False
        _LOG.warning("[ceres plugin] save_on_exit 注册失败（训练继续，仅失去崩溃保护）：%r",
                     _save_exit_e)
