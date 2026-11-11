import copy
import hashlib
import json
from pathlib import Path
import uuid

from accelerate.utils import gather_object
import torch

from ceres_plugin import plugin
from rewards import group_advantages, load_reward_config
from .action_objective import action_grpo_loss
from .action_tokens import teacher_action_ids
from .ceres_grpo_trainer import CeresGRPOTrainer

ACTION_INSTRUCTION = (
    "Emit exactly ONE pedagogical action per assistant turn: <recall>, <probe>, "
    "<hint level=\"1~3\">, <explain>, <correct>, <encourage>, <check>, or <summarize>, "
    "with its closing tag. Wait for the learner's response before the next action. "
    "You may append the control marker <end/> to the final action. Use the learner's language."
)


class CeresActionGRPOTrainer(CeresGRPOTrainer):
    def __init__(self, *args, **kwargs):
        cfg = kwargs["args"]
        if kwargs.get("reward_funcs") != ["ceres_sequence"] or cfg.reward_weights != [1.0]:
            raise ValueError("Action mode uses ONLY ceres_sequence with weight 1; lambda_seq lives in reward config")
        self.ceres_config = load_reward_config()
        super().__init__(*args, **kwargs)
        if self.accelerator.is_main_process:
            path = Path(self.args.output_dir) / "resolved_reward_config.json"
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(self.ceres_config, ensure_ascii=False, indent=2) + "\n")

    def _initialize_rollouts(self, inputs):
        super()._initialize_rollouts(inputs)


        for i, row in enumerate(inputs):
            if any(m.get("role") == "assistant" for m in row["messages"]):
                raise ValueError("Action GRPO requires query-only input, without pre-existing assistant turns")
            # Unique rollout IDs prevent collisions when final responses are identical.
            row["source_uid"] = row.get("uid", "")
            row["uid"] = f"ceres-{uuid.uuid4().hex}-{self.accelerator.process_index}-{i}"
            row["messages"] = copy.deepcopy(row["messages"])
            system = next((m for m in row["messages"] if m.get("role") == "system"), None)
            if system is None:
                row["messages"].insert(0, {"role": "system", "content": ACTION_INSTRUCTION})
            else:
                if not isinstance(system["content"], str):
                    raise ValueError("System message must be text")
                system["content"] += "\n" + ACTION_INSTRUCTION
            context = {k: v for k, v in row.items() if k not in
                       {"uid", "source_uid", "_ceres_context_hash", "is_truncated"} and not k.startswith("_ceres_")}
            row["_ceres_context_hash"] = hashlib.sha256(
                json.dumps(context, ensure_ascii=False, sort_keys=True, default=str).encode()).hexdigest()

    def _align_batch(self, inputs, batch):
        local = []
        completions = [row["messages"][-1]["content"] for row in inputs]
        kwargs = {"uid": [row["uid"] for row in inputs], "messages": [row["messages"] for row in inputs]}
        for i, row in enumerate(inputs):
            rec, _ = plugin._lookup_rec(kwargs, i, completions)
            local.append({"record": rec, "context": row["_ceres_context_hash"]})
        # Action credit requires the complete prompt group across all ranks.
        gathered = gather_object(local)

        for entry in gathered:
            rec = entry["record"]
            if not isinstance(rec, dict):
                raise RuntimeError("Action trajectory missing; refusing zero-reward fallback")
            if rec["reward_config"]["config_sha256"] != self.ceres_config["config_sha256"]:
                raise RuntimeError("Reward configuration changed during a rollout")
        k = self.num_generations
        if len(gathered) % k:
            raise ValueError("Incomplete rollout group")
        mixed, traces = [], []
        for start in range(0, len(gathered), k):
            group = gathered[start:start + k]
            if len({r["context"] for r in group}) != 1:
                raise ValueError("Rollout group contains different learner/query contexts")
            records = [r["record"] for r in group]
            result = group_advantages(records, self.ceres_config)
            mixed.extend(result["mixed"])
            for i, rec in enumerate(records):
                traces.append({"trajectory_id": rec["uid"], "source_uid": rec["source_uid"],
                               "group_id": records[0]["uid"], "reward_components": rec["reward_components"],
                               "sequence_advantage": result["sequence"][i], "suffix": result["suffix"][i],
                               "action_advantages": result["action"][i], "mixed_advantages": result["mixed"][i]})
        offset = self.accelerator.process_index * len(inputs)
        local_mixed = mixed[offset:offset + len(inputs)]
        trace_path = Path(self.args.output_dir) / f"action_credit_rank{self.accelerator.process_index}.jsonl"
        trace_path.parent.mkdir(parents=True, exist_ok=True)
        template = copy.copy(self.template)
        with self._template_context(template):
            encoded = template.data_collator([template.encode(row) for row in inputs])
        if not torch.equal(encoded["input_ids"].cpu(), batch["input_ids"].cpu()):
            raise ValueError("Template encoding changed between log-prob and action alignment")
        owners, values = [], []
        with trace_path.open("a", encoding="utf-8") as trace_file:
            for position, entry in enumerate(local):
                rec = entry["record"]
                ids = teacher_action_ids(encoded["input_ids"][position].tolist(),
                                         encoded["labels"][position].tolist(),
                                         rec["steps"], self.processing_class)
                ids = ids[-batch["logits_to_keep"]:]
                owners.append(ids)
                values.append([local_mixed[position][owner] if owner >= 0 else 0.0 for owner in ids])
                trace = traces[offset + position]
                trace["teacher_token_action_ids"] = ids
                trace_file.write(json.dumps(trace, ensure_ascii=False) + "\n")
        device = batch["input_ids"].device
        batch["ceres_action_ids"] = torch.tensor(owners, dtype=torch.long, device=device)
        batch["advantages"] = torch.tensor(values, dtype=torch.float32, device=device)
        batch["completion_mask"] &= batch["ceres_action_ids"] >= 0
        return batch

    def _get_per_token_logps(self, model, inputs):
        # Training metadata must not reach the model forward method.
        return super()._get_per_token_logps(model, {k: v for k, v in inputs.items()
                                                  if k != "ceres_action_ids"})

    def compute_loss(self, model, inputs, return_outputs=False, num_items_in_batch=None):
        if return_outputs:
            raise ValueError("CERES action GRPO does not return model outputs")
        if isinstance(inputs, list):
            if len(inputs) != 1:
                raise ValueError("Expected one GRPO microbatch")
            inputs = inputs[0]
        logps = self._get_per_token_logps(model, inputs)
        old = inputs["old_per_token_logps"] if self.old_policy else logps.detach()
        mask = inputs["completion_mask"]
        loss, metrics = action_grpo_loss(logps, old, inputs["ref_per_token_logps"], inputs["advantages"],
                                        inputs["ceres_action_ids"], mask,
                                        self.epsilon, self.epsilon, self.beta)
        mode = "train" if self.model.training else "eval"
        self._metrics[mode]["ceres/policy_loss"].append(metrics["policy"].item())
        self._metrics[mode]["ceres/action_kl"].append(metrics["kl"].item())
        self._metrics[mode]["ceres/actions_local"].append(metrics["actions_local"])
        return loss
