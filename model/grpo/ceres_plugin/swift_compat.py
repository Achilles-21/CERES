import hashlib
from importlib.metadata import version
import inspect
from pathlib import Path


def register_trainer(class_path):
    if version("ms-swift") != "3.2.1":
        raise RuntimeError("CERES training requires ms-swift==3.2.1")
    from swift.trainers.rlhf_trainer.grpo_trainer import GRPOTrainer
    from swift.trainers.trainer_factory import TrainerFactory

    source = Path(inspect.getfile(GRPOTrainer)).read_bytes().replace(b"\r\n", b"\n").rstrip(b"\n") + b"\n"
    expected = "9d8e35bdcb44b9d782cb6c35c3fe55be4fe3823991016c2d2a8dee2f0556da4a"
    if hashlib.sha256(source).hexdigest() != expected:
        raise RuntimeError("Installed GRPO trainer differs from the official ms-swift 3.2.1 source")
    TrainerFactory.TRAINER_MAPPING["grpo"] = class_path
