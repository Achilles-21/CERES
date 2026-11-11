# -*- coding: utf-8 -*-
'Teaching-action grammar: per-turn structure and cross-turn ordering constraints.'


import re

__all__ = [
    "TAG_RE",
    "READING_NODES",
    "VALID_TAGS",
    "parse_actions",
    "check_grammar_turn",
    "check_grammar_global",
    "hint_level",
    "validate_sft_trajectory",
    "action_stats",
]


VALID_TAGS = ("recall", "probe", "hint", "check", "explain", "correct", "encourage", "summarize", "end")


READING_NODES = ("ophthalmology/retina", "ophthalmology/neuro_ophth", "ophthalmology/fundus")


TAG_RE = re.compile(
    r"<(?P<tag>recall|probe|hint|check|explain|correct|encourage|summarize|end)"
    r"(?![\w-])"
    r"(?P<attrs>[^<>]*?)"
    r"(?:/>|>(?P<body>.*?)</(?P=tag)>)",
    re.S,
)


_TAG_TOKEN_RE = re.compile(r"<(/?)([A-Za-z_][A-Za-z0-9_\-]*)([^<>]*?)(/?)>")


_LEVEL_RE = re.compile(r"level\s*=\s*[\"']?(\d+)")


def parse_actions(text):
    if not isinstance(text, str):
        return []
    acts = []
    for m in TAG_RE.finditer(text):
        tag = m.group("tag")
        attrs = m.group("attrs") or ""
        body = m.group("body")
        acts.append((tag, attrs, (body if body is not None else "").strip()))
    return acts


def hint_level(attrs):
    if not isinstance(attrs, str):
        return 1
    m = _LEVEL_RE.search(attrs)
    if not m:
        return 1
    try:
        return int(m.group(1))
    except (TypeError, ValueError):
        return 1


def check_grammar_turn(text):
    return 1.0 if not _turn_defects(text) else 0.0


def check_grammar_global(steps, dd=None):
    return not _global_defects(steps, dd)


def validate_sft_trajectory(messages, dd=None):
    if messages is None:
        return False, ["messages 为 None"]
    if isinstance(messages, (str, bytes)) or not isinstance(messages, (list, tuple)):
        return False, ["messages 非法：应为消息 dict 列表"]
    msgs = list(messages)
    if not msgs:
        return False, ["messages 为空列表"]

    reasons = []


    for i, m in enumerate(msgs):
        if not isinstance(m, dict):
            reasons.append("第%d条消息不是 dict" % (i + 1))
            continue
        role = m.get("role")
        if role not in ("user", "assistant"):
            reasons.append("第%d条消息角色非法: %r（仅允许 user/assistant）" % (i + 1, role))
    for i, m in enumerate(msgs):
        want = "user" if i % 2 == 0 else "assistant"
        role = m.get("role") if isinstance(m, dict) else None
        if role != want:
            reasons.append("第%d条消息应为 %s，实际为 %r（要求 user/assistant 交替，"
                           "且以 assistant 收尾）" % (i + 1, want, role))


    last = msgs[-1]
    last_role = last.get("role") if isinstance(last, dict) else None
    if last_role != "assistant":
        reasons.append("最后一条消息应为 assistant（要求以 assistant 收尾）")


    steps = []
    for m in msgs:
        if isinstance(m, dict) and m.get("role") == "assistant":
            txt = _as_text(m.get("content"))
            steps.append({"turn": len(steps), "teacher": txt})
            for defect in _turn_defects(txt):
                reasons.append("G1: 第%d轮 %s" % (len(steps), defect))
    if not steps:
        reasons.append("轨迹为空：未找到 assistant（教师）消息")
        return False, _dedupe(reasons)


    reasons.extend(_global_defects(steps, dd))
    reasons = _dedupe(reasons)
    return (not reasons), reasons


def action_stats(texts):
    if texts is None:
        turns = []
    elif isinstance(texts, str):
        turns = [texts]
    else:
        try:
            turns = list(texts)
        except Exception:
            turns = []

    tag_counts = dict.fromkeys(VALID_TAGS, 0)
    tag_turns = dict.fromkeys(VALID_TAGS, 0)
    hint_levels = []
    has_end = False
    for item in turns:
        text = item if isinstance(item, str) else ""
        seen = set()
        for tag, attrs, _body in parse_actions(text):
            tag_counts[tag] += 1
            seen.add(tag)
            if tag == "hint":
                hint_levels.append(hint_level(attrs))
            elif tag == "end":
                has_end = True
        for tag in seen:
            tag_turns[tag] += 1
    return {
        "n_turns": len(turns),
        "tag_counts": tag_counts,
        "tag_turns": tag_turns,
        "max_hint_level": max(hint_levels) if hint_levels else 0,
        "hint_levels": hint_levels,
        "has_end": bool(has_end),
    }


def _turn_defects(text):
    if not isinstance(text, str):
        return ["教师文本非字符串"]
    if not text.strip():
        return ["教师文本为空"]
    tokens = _TAG_TOKEN_RE.findall(text)
    if not tokens:
        return ["无任何动作标签（要求只用 8 类教学动作及 end 控制标签的结构化输出）"]

    defects = []
    stack = []
    n_actions = 0
    for closing, name, _mid, selfclose in tokens:
        if name not in VALID_TAGS:
            defects.append("未知标签 <%s>（仅允许 8 类教学动作及 end 控制标签）" % name)
            continue
        if name == "end":
            if closing:
                defects.append("出现配对形式的 </end>（<end/> 必须自闭合）")
            elif not selfclose:
                defects.append("<end> 必须写成自闭合 <end/>")
            elif stack:
                defects.append("<end/> 不得嵌套在其它动作标签内部")
            else:
                n_actions += 1
            continue
        if closing:
            if not stack:
                defects.append("多余的闭合标签 </%s>" % name)
            elif stack[-1] != name:
                defects.append("标签配对错误：<%s> 被 </%s> 闭合" % (stack[-1], name))
                stack = []
            else:
                stack.pop()
        elif selfclose:
            defects.append("<%s/> 不允许自闭合（%s 必须携带正文）" % (name, name))
        else:
            if stack:
                defects.append("标签嵌套：<%s> 内出现 <%s>（禁止嵌套）" % (stack[-1], name))
            else:
                stack.append(name)
                n_actions += 1
    if stack:
        defects.append("标签未闭合: " + "、".join("<%s>" % t for t in stack))
    if n_actions == 0 and not defects:
        defects.append("无任何动作标签")
    return defects


def _g5_enforced(dd):
    'Reading order applies to reading nodes except routine clarification.'


    if dd is None or not isinstance(dd, dict):
        return True
    node = dd.get("curriculum_node")
    if not isinstance(node, str) or not node.strip():
        return True
    if not node.startswith(READING_NODES):
        return False
    difficulty = dd.get("difficulty")
    if not isinstance(difficulty, str):
        difficulty = ""
    return difficulty != "routine_clarification"


def _global_defects(steps, dd=None):
    'Check prerequisites before recording the current action to preserve within-turn order.'


    if steps is None:
        return ["steps 为 None"]
    if isinstance(steps, (str, bytes)) or not isinstance(steps, (list, tuple)):
        return ["steps 非法：应为 step dict 列表"]
    step_list = list(steps)
    if not step_list:
        return ["轨迹为空：没有任何教师回合"]

    defects = []
    g5 = _g5_enforced(dd)
    has_scaffold = False
    has_check = False
    last_level = 0
    end_turn = -1

    for i, st in enumerate(step_list):
        teacher = st.get("teacher") if isinstance(st, dict) else None
        if isinstance(teacher, str):
            if not teacher.strip():
                defects.append("第%d轮 teacher 文本为空" % (i + 1))
        else:
            defects.append("第%d轮 teacher 文本缺失或类型非法" % (i + 1))
            teacher = ""

        acts = parse_actions(teacher)


        counts = {}
        for tag, _attrs, _body in acts:
            counts[tag] = counts.get(tag, 0) + 1
        for tag, c in counts.items():
            if c >= 3:
                defects.append("G3: 第%d轮 <%s> 重复 %d 次（同轮同标签 ≥3 次违规）"
                               % (i + 1, tag, c))

        ended_here = False
        for tag, attrs, _body in acts:
            if ended_here:
                defects.append("G4: 第%d轮 <end/> 之后仍出现 <%s>" % (i + 1, tag))
                ended_here = False
            if tag == "end":
                if end_turn < 0:
                    end_turn = i
                ended_here = True
                continue

            if tag == "correct" and not (has_scaffold and has_check):
                if not has_scaffold:
                    defects.append("G2: 第%d轮 <correct> 之前没有任何 <recall>/<hint> 脚手架"
                                   % (i + 1))
                if not has_check:
                    defects.append("G2: 第%d轮 <correct> 之前没有 <check>（学生未参与）"
                                   % (i + 1))
            if tag in ("recall", "hint"):
                has_scaffold = True
            if tag in ("check", "probe"):
                has_check = True
            if tag == "hint":
                level = hint_level(attrs)
                if level < last_level:
                    defects.append("G3: 第%d轮 hint level 由 %d 回退到 %d（应单调不减）"
                                   % (i + 1, last_level, level))
                last_level = level

            if tag in ("explain", "correct") and g5 and not has_check:
                defects.append("G5: 第%d轮 <%s> 之前未出现 <check>"
                               "（阅片任务必须先让学生描述所见）" % (i + 1, tag))


    if end_turn >= 0 and end_turn < len(step_list) - 1:
        defects.append("G4: <end/> 出现在第%d轮，但其后仍有教师回合（只能出现在最后一轮）"
                       % (end_turn + 1))
    return defects


def _as_text(content):
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
    return ""


def _dedupe(reasons):
    return list(dict.fromkeys(reasons))
