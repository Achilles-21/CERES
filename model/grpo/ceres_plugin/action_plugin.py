import os
from pathlib import Path
import sys

root = str(Path(__file__).resolve().parents[1])
if root not in sys.path:
    sys.path.insert(0, root)
if os.environ.get("CERES_CREDIT_MODE", "action") != "action":
    raise ValueError("action_plugin requires CERES_CREDIT_MODE=action")
os.environ["CERES_CREDIT_MODE"] = "action"
from ceres_plugin.swift_compat import register_trainer

register_trainer("trainers.action_grpo_trainer.CeresActionGRPOTrainer")
from ceres_plugin import plugin

if not plugin.SWIFT_AVAILABLE:
    raise RuntimeError("CERES training requires the real ms-swift ORM registry")
