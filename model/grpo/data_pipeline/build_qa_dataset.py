# -*- coding: utf-8 -*-
'Build single-turn QA data while retaining answer options and grading fields.'


import argparse
import os
import re
import sys


_HERE = os.path.dirname(os.path.abspath(__file__))
_ROOT = os.path.dirname(_HERE)
if _ROOT not in sys.path:
    sys.path.insert(0, _ROOT)

from data_pipeline import llm_client as L  # noqa: E402
from data_pipeline.prompts import format_options_block  # noqa: E402

__all__ = ["build_qa_row", "build_qa_rows", "build_stats", "main"]

_QA_KEYS = ("uid", "messages", "gt_label", "gt_answer", "gt_explanation")


_OPT_LINE_RE = re.compile(r"(?m)^\s*([A-Ja-j])\s*[.、:：)）]\s*\S")


def _parse_option_letters(question):
    return {m.group(1).upper() for m in _OPT_LINE_RE.finditer(question or "")}


def _opt_text_regexes(options):
    out = []
    for letter in sorted(options.keys()):
        text = str(options.get(letter) or "").strip()
        if text:
            out.append((str(letter).upper()[:1],
                        re.compile(r"%s\s*[.、:：)）]\s*%s" % (re.escape(str(letter).upper()[:1]),
                                                              re.escape(text)))))
    return out


def _ensure_options(question, row):
    'Keep a complete, line-based option block so answer-letter grading remains valid.'


    question = question if isinstance(question, str) else ""
    opts = row.get("options") if isinstance(row.get("options"), dict) else {}
    block = format_options_block(opts)
    pairs = _opt_text_regexes(opts)
    want = {letter for letter, _rx in pairs}
    if not block or not want:
        return question
    letters = _parse_option_letters(question)
    if want <= letters:
        return question
    hits = [m.start() for letter, rx in pairs for m in [rx.search(question)] if m]
    if len(hits) == len(want):
        return question[:min(hits)].rstrip() + "\n" + block
    if letters:
        question = _OPT_LINE_RE.sub("", question).rstrip()
    return question.rstrip() + "\n" + block


def build_qa_row(row):
    row = row if isinstance(row, dict) else {}
    question = row.get("student_question")
    if not isinstance(question, str) or not question.strip():
        return None
    question = _ensure_options(question.strip(), row)
    return {
        "uid": str(row.get("qid") or row.get("uid") or ""),
        "messages": [{"role": "user", "content": question}],
        "gt_label": str(row.get("gt_letter") or "").strip().upper(),
        "gt_answer": str(row.get("gt_label") or "").strip(),
        "gt_explanation": str(row.get("gt_explanation") or "").strip(),
    }


def build_qa_rows(rows):
    out, seen = [], {}
    for r in rows or []:
        q = build_qa_row(r)
        if q is None:
            sys.stderr.write("[qa-build] 跳过缺 student_question 的行：%r\n"
                             % str(r.get("qid") if isinstance(r, dict) else r)[:60])
            continue
        uid = q["uid"]
        if not uid:
            q["uid"] = uid = "qa-%06d" % (len(out) + 1)
        if uid in seen:
            sys.stderr.write("[qa-build] uid 重复，后到覆盖：%s\n" % uid)
            out[seen[uid]] = q
        else:
            seen[uid] = len(out)
            out.append(q)
    return out


def build_stats(rows):
    rows = rows or []
    n_gt, n_opts, n_hit = 0, 0, 0
    for r in rows:
        if str(r.get("gt_label") or "").strip():
            n_gt += 1
        letters = _parse_option_letters(r.get("messages", [{}])[0].get("content", "")
                                        if isinstance(r.get("messages"), list) else "")
        if len(letters) >= 2:
            n_opts += 1
        if str(r.get("gt_label") or "")[:1] in letters:
            n_hit += 1
    total = len(rows)
    return {"total": total,
            "gt_coverage": round(n_gt / float(total), 4) if total else 0.0,
            "options_embedded_ratio": round(n_opts / float(total), 4) if total else 0.0,
            "gt_in_options_ratio": round(n_hit / float(total), 4) if total else 0.0}


def build_arg_parser():
    p = argparse.ArgumentParser(
        prog="python -m data_pipeline.build_qa_dataset",
        description="构建标准单轮 QA GRPO 集（学生式 MCQ 提问 + gt 标准答案三列）；"
                    "输入应为 rewrite_question --mode mcq 的 candidates")
    p.add_argument("--input", default="data/queries_candidates_cmexam4k_mcq.jsonl",
                   help="mcq candidates 路径")
    p.add_argument("--output", default="data/cmexam4k_qa_grpo.jsonl", help="QA 集输出路径")
    p.add_argument("--limit", type=int, default=0, help="最多处理条数（0=不限）")
    p.add_argument("--mock", action="store_true", help="保留位（本步骤不调 LLM）")
    return p


def main(argv=None):
    args = build_arg_parser().parse_args(argv)
    rows = L.read_jsonl(args.input)
    if not rows:
        sys.stderr.write("[qa-build] 输入为空：%s（先运行 rewrite_question --mode mcq）\n" % args.input)
        return 2
    if args.limit and args.limit > 0:
        rows = rows[:args.limit]
    qa_rows = build_qa_rows(rows)
    stats = build_stats(qa_rows)
    L.write_jsonl(args.output, qa_rows)
    print("[qa-build] qa=%s total=%d gt_coverage=%.1f%% options_embedded=%.1f%% "
          "gt_in_options=%.1f%%"
          % (args.output, stats["total"], stats["gt_coverage"] * 100,
             stats["options_embedded_ratio"] * 100, stats["gt_in_options_ratio"] * 100))
    return 0


if __name__ == "__main__":
    sys.exit(main())
