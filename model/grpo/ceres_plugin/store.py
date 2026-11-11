import datetime
import hashlib
import json
import math
import os
import re
import threading
import warnings

__all__ = ["make_traj_key", "anchor_key", "step_reward", "final_eval", "TrajectoryStore"]

DEFAULT_FLUSH_DIR = "./traj_dump"
_DEFAULT_MASTERY = 0.3
_DEFAULT_ENGAGEMENT = 0.7
_DEFAULT_SEVERITY = 1.0


PHASE = [(name, r"<" + name + r"(?=[\s>])") for name in
         ("recall", "probe", "hint", "check", "explain", "correct", "encourage", "summarize")]


def _to_float(v, default=0.0):
    try:
        f = float(v)
    except (TypeError, ValueError):
        return default
    return f if math.isfinite(f) else default


def make_traj_key(uid: str, last_text: str) -> str:
    'Share this key derivation between trajectory writes and reward lookups.'


    uid_s = "" if uid is None else str(uid)
    text_s = "" if last_text is None else str(last_text)
    return hashlib.sha1(f"{uid_s}||{text_s.strip()}".encode("utf-8", "ignore")).hexdigest()


def _phase_of(text: str) -> str:
    if not isinstance(text, str):
        return "other"
    matches = [(match.start(), name) for name, pat in PHASE
               if (match := re.search(pat, text)) is not None]
    if matches:
        return min(matches)[1]
    return "other"


def anchor_key(dd: dict, state: dict, teacher_text: str) -> str:
    'Build anchor buckets from the learner state before the action.'


    dd = dd if isinstance(dd, dict) else {}
    state = state if isinstance(state, dict) else {}
    m = _to_float(state.get("mastery"), _DEFAULT_MASTERY)
    m_bucket = "L" if m < 0.34 else ("M" if m < 0.67 else "H")
    node = dd.get("curriculum_node", "na")
    node = "na" if node is None or str(node) == "" else str(node)
    mis = state.get("misconception", "none")
    mis = "none" if mis is None or str(mis) == "" else str(mis)


    e = _to_float(state.get("engagement"), _DEFAULT_ENGAGEMENT)
    e_bucket = "L" if e < 0.34 else ("M" if e < 0.67 else "H")
    return json.dumps([node, m_bucket, mis, e_bucket, _phase_of(teacher_text)],
                      ensure_ascii=False, separators=(",", ":"))


def step_reward(state_after: dict, delta: dict, teacher_text: str) -> float:
    state_after = state_after if isinstance(state_after, dict) else {}
    delta = delta if isinstance(delta, dict) else {}
    dm = _to_float(delta.get("mastery"), 0.0)
    de = _to_float(delta.get("engagement"), 0.0)
    fatigue = _to_float(state_after.get("fatigue"), 0.0)
    fatigue_pen = 0.1 * max(0.0, fatigue - 0.6)
    text = teacher_text if isinstance(teacher_text, str) else ""
    redundant = -0.05 if len(re.findall(r"<hint", text)) > 2 else 0.0
    return 0.6 * dm + 0.3 * de + 0.1 * (1 if dm > 0 else 0) - fatigue_pen + redundant


def final_eval(rec) -> dict:
    steps = []
    if isinstance(rec, dict):
        maybe = rec.get("steps")
        if isinstance(maybe, list):
            steps = maybe
    last = {}
    for st in reversed(steps):
        if isinstance(st, dict) and isinstance(st.get("state_after"), dict):
            last = st["state_after"]
            break
    return {"mastery": _to_float(last.get("mastery"), _DEFAULT_MASTERY),
            "engagement": _to_float(last.get("engagement"), _DEFAULT_ENGAGEMENT),
            "misconception_severity": _to_float(last.get("misconception_severity"), _DEFAULT_SEVERITY)}


class TrajectoryStore:
    'Thread-safe primary and UID indexes retain colliding trajectories for history-based lookup.'


    def __init__(self, flush_dir=None):
        self._d = {}
        self._uid = {}
        self._lock = threading.RLock()
        self._flush_dir_override = flush_dir
        self._ensure_dir()


    def _flush_dir(self) -> str:
        if self._flush_dir_override is not None:
            return str(self._flush_dir_override)
        d = os.environ.get("CERES_TRAJ_DIR", DEFAULT_FLUSH_DIR) or DEFAULT_FLUSH_DIR
        return str(d)

    def _ensure_dir(self):
        try:
            os.makedirs(self._flush_dir(), exist_ok=True)
        except Exception as e:
            warnings.warn(f"[ceres store] 轨迹目录创建失败（忽略，flush 时重试）: {e}")

    def flush(self, rec):
        try:
            d = self._flush_dir()
            os.makedirs(d, exist_ok=True)
            path = os.path.join(d, f"traj_{datetime.datetime.now().strftime('%Y%m%d')}.jsonl")
            with open(path, "a", encoding="utf-8") as f:
                f.write(json.dumps(rec, ensure_ascii=False, default=str) + "\n")
            return path
        except Exception as e:
            warnings.warn(f"[ceres store] 轨迹落盘失败（忽略）: {e}")
            return None


    def put(self, key, rec):
        key_s = "" if key is None else str(key)
        with self._lock:
            self._main_append(key_s, rec)
            self._uid_append(rec)

    def get(self, key):
        key_s = "" if key is None else str(key)
        with self._lock:
            lst = self._d.get(key_s)
            return lst[-1] if lst else None

    def get_all(self, key) -> list:
        key_s = "" if key is None else str(key)
        with self._lock:
            lst = self._d.get(key_s)
            return list(lst) if lst else []


    def put_by_uid(self, uid, rec):
        uid_s = "" if uid is None else str(uid)
        if uid_s == "" and isinstance(rec, dict):
            uid_s = str(rec.get("uid", "") or "")
        with self._lock:
            self._uid_append(rec, uid_s)
            tk = rec.get("traj_key") if isinstance(rec, dict) else None
            if tk:
                self._main_append(str(tk), rec)

    def get_by_uid(self, uid) -> list:
        uid_s = "" if uid is None else str(uid)
        with self._lock:
            lst = self._uid.get(uid_s)
            return list(lst) if lst else []

    def clear(self):
        with self._lock:
            self._d.clear()
            self._uid.clear()


    def _main_append(self, key_s, rec):
        'Rewriting the same record is idempotent; distinct colliding records remain separate.'
        lst = self._d.get(key_s)
        if lst is None:
            self._d[key_s] = [rec]
            return
        for i, item in enumerate(lst):
            if item is rec:
                lst[i] = rec
                return
        lst.append(rec)

    def _uid_append(self, rec, uid=None):
        if not isinstance(rec, dict):
            return
        if uid is None:
            uid = str(rec.get("uid", "") or "")
        if uid == "":
            return
        lst = self._uid.setdefault(uid, [])
        for item in lst:
            if item is rec:
                return
        lst.append(rec)
