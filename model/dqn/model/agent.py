from copy import deepcopy
from dataclasses import asdict, dataclass

import numpy as np
import torch
from torch.nn import functional as F

from ..env import EnvConfig, VirtualClassroom
from ..paths import output_path
from .network import HierarchicalQNetwork, encode_states


@dataclass(frozen=True)
class AgentConfig:
    hidden_dim: int = 256
    embedding_dim: int = 8
    gamma: float = 0.99
    learning_rate: float = 0.0003
    batch_size: int = 32
    target_interval: int = 500
    gradient_clip: float = 10.0

    def __post_init__(self):
        if (self.hidden_dim < 1 or self.embedding_dim < 1 or self.batch_size < 2
                or self.target_interval < 1 or self.learning_rate <= 0
                or self.gradient_clip <= 0 or not 0 <= self.gamma <= 1):
            raise ValueError("Invalid DQN hyperparameters.")


class DQNAgent:
    def __init__(self, env_config=None, config=None, device="cpu", seed=42):
        self.env_config, self.config = env_config or EnvConfig(), config or AgentConfig()
        self.device = torch.device(device)
        self.rng = np.random.default_rng(seed)
        self.constraint_env = VirtualClassroom(self.env_config, seed)
        self.online = HierarchicalQNetwork(self.env_config, self.config.hidden_dim,
                                           self.config.embedding_dim).to(self.device)
        self.target = deepcopy(self.online).eval()
        self.target.requires_grad_(False)
        self.optimizer = torch.optim.Adam(self.online.parameters(), lr=self.config.learning_rate)
        self.updates = 0

    @torch.no_grad()
    def q_values(self, state):
        self.online.eval()
        return self.online(*encode_states([state], self.env_config, self.device))[0].cpu().numpy()

    def select_action(self, state, epsilon=0.0):
        if not 0 <= epsilon <= 1:
            raise ValueError("epsilon must lie in [0, 1].")
        mask = self.constraint_env.action_mask(state)
        valid = np.flatnonzero(mask)
        if not len(valid):
            raise ValueError("Terminal states have no recommendation.")
        if self.rng.random() < epsilon:
            return int(self.rng.choice(valid))
        return int(np.where(mask, self.q_values(state), -np.inf).argmax())

    @torch.no_grad()
    def bellman_targets(self, batch):
        values = self.target(*encode_states([t.next_state for t in batch],
                                           self.env_config, self.device))
        masks = torch.as_tensor(np.asarray([t.next_mask for t in batch], dtype=bool), device=self.device)
        dones = torch.tensor([t.done for t in batch], dtype=torch.bool, device=self.device)
        if ((~masks.any(dim=1)) & (~dones)).any():
            raise ValueError("A nonterminal transition must have a valid next action.")
        next_values = values.masked_fill(~masks, -torch.inf).max(dim=1).values
        next_values = torch.where(dones, torch.zeros_like(next_values), next_values)
        rewards = torch.tensor([t.reward for t in batch], dtype=torch.float32, device=self.device)
        return rewards + self.config.gamma * next_values

    def update(self, replay, beta=0.4, offline_fraction=0.5):
        batch, indices, weights = replay.sample(self.config.batch_size, beta, offline_fraction)
        self.online.train()
        values = self.online(*encode_states([t.state for t in batch], self.env_config, self.device))
        actions = torch.tensor([t.action for t in batch], device=self.device)
        predictions = values.gather(1, actions[:, None]).squeeze(1)
        td_errors = self.bellman_targets(batch) - predictions
        loss = (torch.as_tensor(weights, device=self.device) * td_errors.square()).mean()
        self._optimize(loss)
        replay.update_priorities(indices, td_errors.detach().cpu().numpy())
        self.updates += 1
        if self.updates % self.config.target_interval == 0:
            self.sync_target()
        return {"loss": float(loss.detach()), "mean_abs_td": float(td_errors.detach().abs().mean()),
                "updates": self.updates}

    def behavior_clone(self, demonstrations, steps=200):
        if steps < 0 or (steps and not demonstrations):
            raise ValueError("Behavior cloning requires successful demonstrations.")
        losses = []
        self.online.train()
        for _ in range(steps):
            indices = self.rng.integers(len(demonstrations), size=self.config.batch_size)
            batch = [demonstrations[i] for i in indices]
            logits = self.online(*encode_states([t.state for t in batch], self.env_config, self.device))
            mask = torch.as_tensor(np.asarray([t.mask for t in batch], dtype=bool), device=self.device)
            labels = torch.tensor([t.action for t in batch], device=self.device)
            loss = F.cross_entropy(logits.masked_fill(~mask, -torch.inf), labels)
            self._optimize(loss)
            losses.append(float(loss.detach()))
        self.sync_target()
        return losses

    def _optimize(self, loss):
        if not torch.isfinite(loss):
            raise FloatingPointError("Nonfinite training loss.")
        self.optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(self.online.parameters(), self.config.gradient_clip,
                                      error_if_nonfinite=True)
        self.optimizer.step()

    def sync_target(self):
        self.target.load_state_dict(self.online.state_dict())

    def save(self, path, metadata=None):
        torch.save({"format_version": 1, "env_config": self.env_config.to_dict(),
                    "agent_config": asdict(self.config), "online": self.online.state_dict(),
                    "target": self.target.state_dict(), "optimizer": self.optimizer.state_dict(),
                    "updates": self.updates, "metadata": metadata or {}}, output_path(path))

    @classmethod
    def load(cls, path, device="cpu"):
        checkpoint = torch.load(path, map_location=device, weights_only=True)
        if checkpoint["format_version"] != 1:
            raise ValueError("Unsupported checkpoint format.")
        agent = cls(EnvConfig.from_dict(checkpoint["env_config"]),
                    AgentConfig(**checkpoint["agent_config"]), device=device)
        agent.online.load_state_dict(checkpoint["online"])
        agent.target.load_state_dict(checkpoint["target"])
        agent.optimizer.load_state_dict(checkpoint["optimizer"])
        agent.updates = checkpoint["updates"]
        agent.online.eval()
        return agent
