import copy

import torch


class TeacherRolloutEngine:
    def __init__(self, engine, environment, accelerator, max_turns):
        self.engine = engine
        self.environment = environment
        self.accelerator = accelerator
        self.max_turns = max_turns

    def infer(self, infer_requests, request_config, use_tqdm=False):
        rows = infer_requests
        if not rows:
            raise ValueError("CERES rollout requires a nonempty local batch")
        final_outputs = [None] * len(rows)
        # Finished rows still generate placeholders to keep rank-local batch sizes aligned.
        for _ in range(self.max_turns):
            requests = []
            for row in rows:
                request = {key: copy.deepcopy(value) for key, value in row.items()
                           if not key.startswith("_ceres_") and key not in {"finished", "finish_reason"}}
                if request["messages"][-1]["role"] == "assistant":
                    request["messages"].pop()
                requests.append(request)
            outputs = self.engine.infer(requests, request_config, use_tqdm=use_tqdm)
            if len(outputs) != len(rows):
                raise ValueError("Teacher engine returned an incomplete local batch")
            active = []
            for index, (row, output) in enumerate(zip(rows, outputs)):
                if row.get("finished"):
                    continue
                choice = output.choices[0]
                row["messages"].append({"role": "assistant", "content": choice.message.content or ""})
                row["finish_reason"] = choice.finish_reason
                final_outputs[index] = output
                active.append(row)
            if active:
                returned = self.environment(active)
                if len(returned) != len(active) or any(a is not b for a, b in zip(returned, active)):
                    raise ValueError("Teacher environment must preserve rollout rows and their order")
            # All ranks must stop together to keep ZeRO collectives aligned.
            remaining = torch.tensor(int(any(not row.get("finished") for row in rows)),
                                     device=self.accelerator.device)
            remaining = self.accelerator.reduce(remaining, reduction="sum")
            if remaining.item() == 0:
                if any(output is None for output in final_outputs):
                    raise RuntimeError("Teacher rollout finished without a model response")
                return final_outputs
        raise RuntimeError("Teacher environment did not finish within CERES_MAX_TURNS")

    def __getattr__(self, name):
        return getattr(self.engine, name)
