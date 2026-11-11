import copy

from swift.trainers.rlhf_trainer.grpo_trainer import GRPOTrainer

from ceres_plugin import plugin
from .rollout import TeacherRolloutEngine


class CeresGRPOTrainer(GRPOTrainer):
    def __init__(self, *args, **kwargs):
        config = kwargs["args"]
        for name in ("async_generate", "use_vllm", "use_lmdeploy"):
            if getattr(config, name, False):
                raise ValueError(f"CERES ms-swift 3.2.1 requires synchronous pt inference; disable {name}")
        if getattr(config, "tensor_parallel_size", 1) != 1:
            raise ValueError("CERES teacher rollout requires tensor_parallel_size=1")
        super().__init__(*args, **kwargs)

    def _initialize_rollouts(self, inputs):
        for row in inputs:
            if not row.get("messages") or any(message.get("role") == "assistant" for message in row["messages"]):
                raise ValueError("CERES GRPO requires query-only input without assistant turns")
            for key in list(row):
                if key.startswith("_ceres_") or key in {"finished", "finish_reason"}:
                    row.pop(key)

    def _generate_and_score_completions(self, inputs):
        inputs = copy.deepcopy(inputs)
        self._initialize_rollouts(inputs)
        engine = self.engine
        self.engine = TeacherRolloutEngine(engine, plugin.teacher_env, self.accelerator, plugin._max_turns())
        try:
            batch = super()._generate_and_score_completions(inputs)
        finally:
            self.engine = engine
        return self._align_batch(inputs, batch)

    def _align_batch(self, inputs, batch):
        return batch
