import os
from pathlib import Path
import sys

root = str(Path(__file__).resolve().parents[1])
if root not in sys.path:
    sys.path.insert(0, root)
if os.environ.get("CERES_CREDIT_MODE", "scalar") != "scalar":
    raise ValueError("scalar_plugin requires CERES_CREDIT_MODE=scalar")
os.environ["CERES_CREDIT_MODE"] = "scalar"

from ceres_plugin.swift_compat import register_trainer

register_trainer("trainers.ceres_grpo_trainer.CeresGRPOTrainer")
from ceres_plugin import plugin

if not plugin.SWIFT_AVAILABLE:
    raise RuntimeError("CERES training requires the real ms-swift ORM registry")
