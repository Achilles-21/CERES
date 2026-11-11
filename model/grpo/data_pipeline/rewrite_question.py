# -*- coding: utf-8 -*-
'Rewrite source questions into learner queries with language-aware fallbacks.'


import argparse
import os
import re
import sys


_HERE = os.path.dirname(os.path.abspath(__file__))
_ROOT = os.path.dirname(_HERE)
if _ROOT not in sys.path:
    sys.path.insert(0, _ROOT)

from data_pipeline import llm_client as L  # noqa: E402
from data_pipeline.prompts import (  # noqa: E402
    DIFFICULTY_LEVELS,
    format_options_block,
    persona_profile_prompt,
    persona_user_prompt,
    rewrite_system_prompt,
    rewrite_user_prompt,
)

__all__ = ["DIFFICULTY_HINT_MAP", "clean_option_traces", "degrade_question",
           "profile_template", "misconception_from_wrong", "difficulty_from_hint",
           "row_lang", "is_general_row", "rewrite_rows", "main"]


DIFFICULTY_HINT_MAP = {
    "low": "routine_clarification",
    "mid": "misconception_triggering",
    "high": "difficult_diagnostic",
    "低": "routine_clarification",
    "中": "misconception_triggering",
    "高": "difficult_diagnostic",
}


_PROFILE_TEMPLATE = {
    "routine_clarification": "眼科规培一年级，基础概念尚不牢固，常混淆相近名词，目标是通过日常答疑补齐概念边界。",
    "misconception_triggering": "眼科规培一年级，能复述结论但概念边界模糊，容易把单一征象当成充分条件，目标是建立规范判读顺序。",
    "difficult_diagnostic": "眼科规培二年级，有一定阅片量，正在建立系统的鉴别诊断与急症处置思路，目标是独立完成病例分析。",
}
_PROFILE_TEMPLATE_EN = {
    "routine_clarification": "First-year ophthalmology resident; basic concepts still shaky, often "
                             "confuses neighboring terms, aiming to firm up concept boundaries "
                             "through daily Q&A.",
    "misconception_triggering": "First-year ophthalmology resident; can recite conclusions but the "
                                "boundaries are blurry, tends to treat a single sign as sufficient, "
                                "aiming to build a standard reading order.",
    "difficult_diagnostic": "Second-year ophthalmology resident with moderate reading volume, "
                            "building systematic differential-diagnosis and emergency-management "
                            "thinking, aiming to work up cases independently.",
}


_PROFILE_TEMPLATE_GENERAL = {
    "routine_clarification": "临床规培一年级，正在内科/外科等科室轮转，基础概念尚不牢固，常混淆相近名词，目标是通过日常答疑补齐概念边界。",
    "misconception_triggering": "临床规培一年级，能复述结论但概念边界模糊，容易把单一征象或单一检查当成充分条件，目标是建立规范的鉴别思路。",
    "difficult_diagnostic": "临床规培二年级，有一定病例量，正在建立系统的鉴别诊断与急症处置思路，目标是独立完成病例分析。",
}
_PROFILE_TEMPLATE_EN_GENERAL = {
    "routine_clarification": "First-year clinical resident on rotation; basic concepts still shaky, "
                             "often confuses neighboring terms, aiming to firm up concept "
                             "boundaries through daily Q&A.",
    "misconception_triggering": "First-year clinical resident; can recite conclusions but the "
                                "boundaries are blurry, tends to treat a single sign or single test "
                                "as sufficient, aiming to build a standard diagnostic approach.",
    "difficult_diagnostic": "Second-year clinical resident with moderate case volume, building "
                            "systematic differential-diagnosis and emergency-management thinking, "
                            "aiming to work up cases independently.",
}


_OPT_LINE_RE = re.compile(r"(?:^|\n)\s*[A-Ea-e]\s*[.、:：)）]\s*[^\n]*")
_SELECT_PHRASE_RE = re.compile(r"(?:下列|以下)(?:哪[一项项个种]*|那一项)?")
_SELECT_PHRASE_RE_EN = re.compile(r"which\s+(?:one\s+)?of\s+the\s+following", re.IGNORECASE)


_OPTION_TRACE_RE = re.compile(r"(?:^|\n|\s)[A-E][.、:：)）]|下列哪|以下哪")


_OPTION_TRACE_RE_EN = re.compile(
    r"(?m)^\s*[A-E]\s*[.、:：)）]\s*\S|which\s+of\s+the\s+following", re.IGNORECASE)


def _norm_lang(lang):
    return "en" if isinstance(lang, str) and lang.strip().lower() == "en" else "zh"


def row_lang(row):
    row = row if isinstance(row, dict) else {}
    return _norm_lang(row.get("lang"))


def clean_option_traces(stem, lang="zh"):
    t = stem if isinstance(stem, str) else ""
    t = _OPT_LINE_RE.sub("", t)
    if _norm_lang(lang) == "zh":
        t = _SELECT_PHRASE_RE.sub("这个知识点中", t)
    else:
        t = _SELECT_PHRASE_RE_EN.sub("this concept", t)
    t = re.sub(r"\s*\n\s*", " ", t).strip()
    return t


def degrade_question(row, lang=None, mode="open"):
    row = row if isinstance(row, dict) else {}
    lg = _norm_lang(row.get("lang") if lang is None else lang)
    if mode == "mcq":
        block = format_options_block(row.get("options"))
        if not block:
            return degrade_question(row, lang=lg, mode="open")
        stem = str(row.get("stem") or row.get("gt_label") or "这道题").strip()
        prefix = "<image>" if (bool(row.get("image")) or row.get("question_type") == "image") else ""
        if lg == "en":
            return prefix + ("Dr., I'm stuck on this question: %s\n%s\nI've tried ruling the "
                             "options out on my own but keep second-guessing — could you walk me "
                             "through how to judge each option, step by step?" % (stem, block))
        return prefix + ("老师，我在做题时卡住了：%s\n%s\n我按自己的想法排除了一圈还是拿不准，"
                         "能带我把每个选项的判断思路一步步过一遍吗？" % (stem, block))
    if lg == "en":
        stem = clean_option_traces(str(row.get("stem") or row.get("gt_label")
                                       or "this question"), lang="en")
        is_img = bool(row.get("image")) or row.get("question_type") == "image"
        prefix = "<image>" if is_img else ""
        body = ("Dr., while reviewing my %s I got stuck on this: %s. I keep second-guessing "
                "myself when I try to judge it on my own — could you walk me through it "
                "step by step?" % ("reading of this image" if is_img else "notes", stem))
        return prefix + body
    stem = clean_option_traces(str(row.get("stem") or row.get("gt_label") or "这道题"))
    is_img = bool(row.get("image")) or row.get("question_type") == "image"
    prefix = "<image>" if is_img else ""
    body = ("老师，我在看%s时遇到一个问题：%s。我自己判断时总有点拿不准，"
            "能带我从头梳理一遍思路吗？" % ("这张图像" if is_img else "课堂笔记", stem))
    return prefix + body


def profile_template(difficulty, lang="zh", general=False):
    if general:
        table = _PROFILE_TEMPLATE_EN_GENERAL if _norm_lang(lang) == "en" else _PROFILE_TEMPLATE_GENERAL
    else:
        table = _PROFILE_TEMPLATE_EN if _norm_lang(lang) == "en" else _PROFILE_TEMPLATE
    return table.get(difficulty, table["misconception_triggering"])


def is_general_row(row):
    row = row if isinstance(row, dict) else {}
    node = str(row.get("curriculum_node") or "").strip()
    return bool(node) and not node.startswith("ophthalmology")


def misconception_from_wrong(row, lang=None):
    row = row if isinstance(row, dict) else {}
    lg = _norm_lang(row.get("lang") if lang is None else lang)
    wrong = row.get("wrong_options")
    gt = str(row.get("gt_label") or "").strip()
    if isinstance(wrong, (list, tuple)) and wrong:
        first = str(wrong[0] or "").strip()
        if first:
            if lg == "en":
                base = 'mistakenly believes the answer is "%s"' % first
                return base + (' while overlooking "%s"' % gt if gt else "")
            base = "误以为正确答案是「%s」" % first
            return base + ("而忽略「%s」" % gt if gt else "")
    return ""


def difficulty_from_hint(hint, llm_value=None):
    if isinstance(llm_value, str) and llm_value.strip() in DIFFICULTY_LEVELS:
        return llm_value.strip()
    h = str(hint or "").strip().lower()
    if h in DIFFICULTY_HINT_MAP:
        return DIFFICULTY_HINT_MAP[h]
    return "misconception_triggering"


def _build_courseware(row, lang=None):
    row = row if isinstance(row, dict) else {}
    lg = _norm_lang(row.get("lang") if lang is None else lang)
    node = str(row.get("curriculum_node") or "")
    general = is_general_row(row)
    if general:
        subject = "临床医学" if lg != "en" else "Clinical medicine"
        topic = "综合问答" if lg != "en" else "general Q&A"
    else:
        subject = "眼科学" if lg != "en" else "Ophthalmology"
        topic = node.rsplit("/", 1)[-1].replace("_", " ") if node else "眼科答疑"
    if lg == "en":
        topic = topic if node else "ophthalmology Q&A"
        if row.get("question_type") == "image" or row.get("image"):
            text = "%s · %s (Findings: %s)" % (
                subject, topic, str(row.get("finding_text") or "(no description)"))
        else:
            text = "%s · %s (Key points: %s; Explanation: %s)" % (
                subject, topic, str(row.get("stem") or ""),
                str(row.get("gt_explanation") or row.get("gt_label") or ""))
        return text[:600]
    if row.get("question_type") == "image" or row.get("image"):
        parts = ["%s·%s（检查所见：%s）"
                 % (subject, topic, str(row.get("finding_text") or "（无描述）"))]
    else:
        parts = ["%s·%s（题目要点：%s；解析：%s）"
                 % (subject, topic, str(row.get("stem") or ""),
                    str(row.get("gt_explanation") or row.get("gt_label") or ""))]
    return parts[0][:600]


def rewrite_rows(rows, mock=False, use_cache=True, concurrency=None, seed=0, mode="open"):
    rows = [r for r in (rows or []) if isinstance(r, dict)]
    if not rows:
        return []
    for r in rows:
        if not isinstance(r.get("persona_seed"), int):
            r["persona_seed"] = L.persona_seed_from_qid(r.get("qid"))


    batch = [[{"role": "system", "content": rewrite_system_prompt(row_lang(r), mode=mode)},
              {"role": "user", "content": rewrite_user_prompt(r, lang=row_lang(r), mode=mode)}]
             for r in rows]
    outs = L.chat_many(batch, seeds=[seed + i for i in range(len(batch))],
                       json_mode=True, mock=mock, use_cache=use_cache,
                       concurrency=concurrency)


    def _lang_mismatch(r, obj):
        q = obj.get("student_question") if isinstance(obj, dict) else None
        return isinstance(q, str) and bool(q.strip()) and L.detect_lang(q) != row_lang(r)

    retry_jobs = [(i, r) for i, r in enumerate(rows)
                  if _lang_mismatch(r, L.parse_json_dict(outs[i] if i < len(outs) else ""))]
    if retry_jobs:
        rbatch, rseeds = [], []
        for i, r in retry_jobs:
            lg = row_lang(r)
            nudge = ("IMPORTANT: write student_question in English." if lg == "en"
                     else "注意：student_question 必须用中文书写。")
            rbatch.append([{"role": "system",
                            "content": rewrite_system_prompt(lg, mode=mode) + "\n" + nudge},
                           {"role": "user", "content": rewrite_user_prompt(r, lang=lg, mode=mode)}])
            rseeds.append(seed + 2000000 + i)
        routs = L.chat_many(rbatch, seeds=rseeds, json_mode=True,
                            mock=mock, use_cache=use_cache, concurrency=concurrency)
        for (i, _r), out in zip(retry_jobs, routs):
            outs[i] = out or outs[i]


    need_profile = []
    for i, r in enumerate(rows):
        obj = L.parse_json_dict(outs[i] if i < len(outs) else "")
        profile = obj.get("learner_profile") if isinstance(obj, dict) else None
        if not (isinstance(profile, str) and profile.strip()):
            need_profile.append((i, r))
    if need_profile:
        pbatch = [[{"role": "system", "content": persona_profile_prompt(row_lang(r),
                                                                        general=is_general_row(r))},
                   {"role": "user", "content": persona_user_prompt({
                       "stem": r.get("stem"), "gt_label": r.get("gt_label"),
                       "difficulty_hint": r.get("difficulty_hint"),
                       "misconception_seed": r.get("misconception_seed")},
                       lang=row_lang(r))}]
                  for _i, r in need_profile]
        pouts = L.chat_many(pbatch, seeds=[seed + 10000 + i for i in range(len(pbatch))],
                            json_mode=True, mock=mock, use_cache=use_cache,
                            concurrency=concurrency)
        for j, (i, r) in enumerate(need_profile):
            obj = L.parse_json_dict(pouts[j] if j < len(pouts) else "")
            profile = obj.get("learner_profile") if isinstance(obj, dict) else None
            if isinstance(profile, str) and profile.strip() and not (obj.get("mock")):
                r["_profile_fallback_ok"] = profile.strip()


    out_rows = []
    for i, r in enumerate(rows):
        lg = row_lang(r)
        obj = L.parse_json_dict(outs[i] if i < len(outs) else "")
        question = obj.get("student_question") if isinstance(obj, dict) else None
        rewrote = isinstance(question, str) and bool(question.strip())
        if rewrote and L.detect_lang(question) != lg:
            rewrote = False
        difficulty = difficulty_from_hint(r.get("difficulty_hint"),
                                          obj.get("difficulty") if isinstance(obj, dict) else None)
        if rewrote:
            question = question.strip()
            if mode != "mcq":
                trace_re = _OPTION_TRACE_RE_EN if lg == "en" else _OPTION_TRACE_RE
                question = trace_re.sub("", question).strip() or question
        else:
            question = degrade_question(r, mode=mode)

        if (r.get("image") or r.get("question_type") == "image") and not question.startswith("<image>"):
            question = "<image>" + question

        profile = ""
        if isinstance(obj, dict):
            p = obj.get("learner_profile")
            if isinstance(p, str) and p.strip():
                profile = p.strip()
        if not profile:
            profile = r.pop("_profile_fallback_ok", "") or profile_template(
                difficulty, lang=lg, general=is_general_row(r))

        mis = ""
        if isinstance(obj, dict):
            m = obj.get("misconception_seed")
            if isinstance(m, str) and m.strip():
                mis = m.strip()
        if not mis:
            mis = misconception_from_wrong(r)

        row_out = {
            "qid": r.get("qid") or "",
            "source": r.get("source") or "",
            "question_type": r.get("question_type") or "text",
            "image": r.get("image"),
            "images": [r["image"]] if r.get("image") else [],
            "student_question": question,
            "learner_profile": profile,
            "misconception_seed": mis,
            "difficulty": difficulty,
            "persona_seed": int(r.get("persona_seed") or 0),
            "curriculum_node": r.get("curriculum_node") or "ophthalmology/general/ophthalmology_qa",
            "courseware_context": _build_courseware(r),
            "gt_label": str(r.get("gt_label") or ""),
            "gt_letter": str(r.get("gt_letter") or "").upper(),
            "gt_explanation": str(r.get("gt_explanation") or ""),
            "options": r.get("options") if isinstance(r.get("options"), dict) else {},
            "finding_text": str(r.get("finding_text") or ""),
            "stem": str(r.get("stem") or ""),
            "rewrote": bool(rewrote),
            "lang": lg,
        }
        out_rows.append(row_out)
    return out_rows


def build_arg_parser():
    p = argparse.ArgumentParser(
        prog="python -m data_pipeline.rewrite_question",
        description="Step 2：raw_pool → queries_candidates（考题改写为课堂学生提问 + 附加列；"
                    "语言逐行跟随 raw_pool 的 lang 列）")
    p.add_argument("--input", default="data/raw_pool.jsonl", help="raw_pool 路径")
    p.add_argument("--output", default="data/queries_candidates.jsonl", help="候选查询输出路径")
    p.add_argument("--limit", type=int, default=0, help="最多处理条数（0=不限）")
    p.add_argument("--mock", action="store_true", help="强制离线 mock（默认：key 未设时自动）")
    p.add_argument("--no-cache", action="store_true", help="关闭 LLM 缓存")
    p.add_argument("--seed", type=int, default=0, help="采样基准 seed")
    p.add_argument("--concurrency", type=int, default=None, help="覆写并发上限")
    p.add_argument("--mode", choices=["open", "mcq"], default="open",
                   help="open=去选项痕迹的课堂提问（默认）；mcq=保留 A–E 选项的学生提问"
                        "（QA GRPO 用，配 build_qa_dataset）")
    return p


def main(argv=None):
    args = build_arg_parser().parse_args(argv)
    rows = L.read_jsonl(args.input)
    if not rows:
        sys.stderr.write("[rewrite] 输入为空：%s（先运行 extract_questions）\n" % args.input)
        return 2
    if args.limit and args.limit > 0:
        rows = rows[:args.limit]
    out = rewrite_rows(rows, mock=args.mock, use_cache=not args.no_cache,
                       concurrency=args.concurrency, seed=args.seed, mode=args.mode)
    L.write_jsonl(args.output, out)
    n_ok = sum(1 for r in out if r.get("rewrote"))
    n_img = sum(1 for r in out if r.get("images"))
    n_en = sum(1 for r in out if r.get("lang") == "en")
    print("[rewrite] mode=%s candidates=%s total=%d rewrote=%d degraded=%d image=%d en=%d"
          % (args.mode, args.output, len(out), n_ok, len(out) - n_ok, n_img, n_en))
    return 0


if __name__ == "__main__":
    sys.exit(main())
