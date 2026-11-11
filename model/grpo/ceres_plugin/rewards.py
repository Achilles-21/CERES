import hashlib
import json
import math
import os
from pathlib import Path

from grammar import _global_defects, _turn_defects, parse_actions

DEFAULT_CONFIG = Path(__file__).resolve().parents[1] / "configs/rewards/default.json"


def load_reward_config():
    path = Path(os.environ.get("CERES_REWARD_CONFIG") or DEFAULT_CONFIG)
    cfg = json.loads(path.read_text(encoding="utf-8"))
    for name in ("alpha", "beta", "gamma", "mu1", "mu2", "mu3", "lambda_fmt",
                 "lambda_loop", "gamma_d", "lambda_seq", "epsilon", "risk_weight", "end_penalty"):
        value = cfg[name]
        if isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value) or value < 0:
            raise ValueError(f"Invalid reward coefficient: {name}={value!r}")
    if not 0 <= cfg["gamma_d"] <= 1 or not 0 <= cfg["lambda_seq"] <= 1 or cfg["epsilon"] == 0:
        raise ValueError("gamma_d/lambda_seq must be in [0,1]; epsilon must be positive")
    if cfg["step_reward_source"] != "terminal_sequence_reward":
        raise ValueError("Supported step-reward allocation: terminal_sequence_reward")
    for pair, value in cfg["rho"].items():
        if len(pair.split(">")) != 2 or isinstance(value, bool) or not math.isfinite(float(value)):
            raise ValueError(f"Invalid rho: {pair}")

    for env, key in (("CERES_END_PENALTY", "end_penalty"), ("CERES_RISK_WEIGHT", "risk_weight")):
        if env in os.environ:
            value = float(os.environ[env])
            if not math.isfinite(value) or value < 0:
                raise ValueError(f"Invalid {env}")
            cfg[key] = value
    cfg["config_sha256"] = hashlib.sha256(
        json.dumps(cfg, sort_keys=True, separators=(",", ":")).encode()).hexdigest()
    return cfg


def _number(state, key):
    value = state.get(key)
    if isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value):
        raise ValueError(f"Missing/invalid learner state: {key}")
    if not 0 <= value <= 1:
        raise ValueError(f"Learner state outside [0,1]: {key}")
    return float(value)


def sequence_components(rec, cfg, risk_patterns=()):
    'Keep ordering rewards as sums and learning rewards as state changes; do not clip or renormalize.'


    steps = rec["steps"]
    if not steps:
        raise ValueError("Cannot score an empty trajectory")
    defects = [d for step in steps for d in _turn_defects(step["teacher"])]
    defects += _global_defects(steps, rec.get("context", {}))
    if rec.get("state_grammar_violation"):
        defects.append("end without correction or resolved misconception")
    defects += rec.get("rollout_defects", [])


    actions = [a for step in steps if not step.get("invalid_bundle")
               for a in parse_actions(step["teacher"]) if a[0] != "end"]
    if not actions:
        defects.append("no pedagogical action")
    fmt = float(not defects) - cfg["lambda_fmt"] * len(defects)
    pairs = sum(float(cfg["rho"].get(left[0] + ">" + right[0], 0.0))
                for i, left in enumerate(actions) for right in actions[i + 1:])
    signatures = [(tag, attrs.strip(), body.strip()) for tag, attrs, body in actions]
    loops = len(signatures) - len(set(signatures))
    rule = pairs - cfg["lambda_loop"] * loops
    before, after = steps[0]["state_before"], steps[-1]["state_after"]
    dm = _number(after, "mastery") - _number(before, "mastery")
    de = _number(after, "engagement") - _number(before, "engagement")
    severity = _number(after, "misconception_severity")
    learn = cfg["mu1"] * dm + cfg["mu2"] * de - cfg["mu3"] * severity
    text = "\n".join(step["teacher"] for step in steps)
    risk = float(any(p in text for p in risk_patterns))
    end = cfg["end_penalty"] if rec.get("finished_reason") != "end_tag" else 0.0
    weighted = {"format": cfg["alpha"] * fmt, "rule": cfg["beta"] * rule,
                "learn": cfg["gamma"] * learn, "risk": -cfg["risk_weight"] * risk,
                "termination": -end}
    return {"format": fmt, "rule": rule, "learn": learn, "mastery_gain": dm,
            "engagement_gain": de, "residual_severity": severity, "violations": defects,
            "ordering_pairs": pairs, "loops": loops, "weighted": weighted,
            "total": sum(weighted.values()), "reward_version": cfg["reward_version"],
            "config_sha256": cfg["config_sha256"]}


def suffix_returns(rewards, discount):
    suffix, acc = [0.0] * len(rewards), 0.0
    for i in range(len(rewards) - 1, -1, -1):
        acc = float(rewards[i]) + discount * acc
        suffix[i] = acc
    return suffix


def standardize(values, epsilon):
    if not values:
        return []
    mean = sum(values) / len(values)
    std = math.sqrt(sum((v - mean) ** 2 for v in values) / len(values))
    return [(v - mean) / (std + epsilon) for v in values]


def group_advantages(records, cfg):
    'Normalize one complete rollout group, weighting repeated anchor visits individually.\nControl-only endings inherit credit from the preceding teaching action.'


    seq = standardize([r["reward_components"]["total"] for r in records], cfg["epsilon"])
    suffix = [suffix_returns([s["step_reward"] for s in r["steps"]], cfg["gamma_d"])
              for r in records]
    act = [[0.0] * len(r["steps"]) for r in records]
    buckets = {}
    for i, rec in enumerate(records):
        for l, step in enumerate(rec["steps"]):
            if step.get("credit_step", l) != l:
                continue
            buckets.setdefault(step["anchor"], []).append((i, l, suffix[i][l]))
    for items in buckets.values():
        for (i, l, _), value in zip(items, standardize([x[2] for x in items], cfg["epsilon"])):
            act[i][l] = value
    for i, rec in enumerate(records):
        for l, step in enumerate(rec["steps"]):
            act[i][l] = act[i][step.get("credit_step", l)]
    mixed = [[cfg["lambda_seq"] * seq[i] + (1 - cfg["lambda_seq"]) * a for a in row]
             for i, row in enumerate(act)]
    return {"sequence": seq, "suffix": suffix, "action": act, "mixed": mixed}
