from dataclasses import dataclass
from collections import deque

import numpy as np

from ..env import LearnerState


@dataclass(frozen=True)
class Transition:
    state: LearnerState
    action: int
    reward: float
    next_state: LearnerState
    terminated: bool
    truncated: bool
    mask: tuple
    next_mask: tuple
    successful: bool = False

    @property
    def done(self):


        return self.terminated or self.truncated


class PrioritizedReplay:


    def __init__(self, capacity=5000, alpha=0.6, seed=42, source_limits=None):
        if capacity < 1 or not 0 <= alpha <= 1:
            raise ValueError("Invalid replay capacity or alpha.")
        self.capacity, self.alpha = capacity, alpha
        self.rng = np.random.default_rng(seed)
        self.items, self.sources = [], []
        self.priorities = np.zeros(capacity, dtype=np.float64)
        self.cursor = 0
        self.source_limits = source_limits
        if source_limits is not None and (set(source_limits) != {"offline", "online"}
                or any(v < 1 for v in source_limits.values())
                or sum(source_limits.values()) != capacity):
            raise ValueError("Source quotas must be positive and sum to the replay capacity.")
        self.source_queues = {source: deque() for source in ("offline", "online")}

    def __len__(self):
        return len(self.items)

    def add(self, item, source="offline"):
        if source not in ("offline", "online"):
            raise ValueError("Unknown replay source.")
        priority = max(1.0, float(self.priorities[:len(self)].max())) if len(self) else 1.0
        if self.source_limits is not None:
            queue = self.source_queues[source]
            if len(queue) == self.source_limits[source]:
                index = queue.popleft()
                self.items[index], self.sources[index] = item, source
            else:
                index = len(self.items)
                self.items.append(item)
                self.sources.append(source)
            queue.append(index)
            self.priorities[index] = priority
            return
        if len(self) < self.capacity:
            self.items.append(item)
            self.sources.append(source)
        else:
            self.items[self.cursor] = item
            self.sources[self.cursor] = source
        self.priorities[self.cursor] = priority
        self.cursor = (self.cursor + 1) % self.capacity

    def sample(self, batch_size=32, beta=0.4, offline_fraction=0.5):
        if not len(self) or batch_size < 1 or not 0 <= beta <= 1:
            raise ValueError("Nonempty replay, positive batch size, and beta in [0, 1] required.")
        if not 0 < offline_fraction < 1:
            raise ValueError("offline_fraction must be strictly between 0 and 1.")
        mass = self.priorities[:len(self)] ** self.alpha
        offline = np.flatnonzero(np.asarray(self.sources) == "offline")
        online = np.flatnonzero(np.asarray(self.sources) == "online")
        if len(offline) and len(online):
            if batch_size < 2:
                raise ValueError("Mixed replay requires a batch of at least two.")
            old_count = min(batch_size - 1, max(1, round(batch_size * offline_fraction)))
            groups = ((offline, old_count), (online, batch_size - old_count))
        else:
            groups = ((np.arange(len(self)), batch_size),)
        indices, probabilities = [], []
        for group, count in groups:
            local_prob = mass[group] / mass[group].sum()
            local_indices = self.rng.choice(len(group), count, replace=True, p=local_prob)
            indices.extend(group[local_indices])

            probabilities.extend((count / batch_size) * local_prob[local_indices])
        order = self.rng.permutation(batch_size)
        indices = np.asarray(indices, dtype=np.int64)[order]
        weights = (len(self) * np.asarray(probabilities)[order]) ** (-beta)
        weights /= weights.max()
        return [self.items[i] for i in indices], indices, weights.astype(np.float32)

    def update_priorities(self, indices, td_errors):
        if len(indices) != len(td_errors) or not np.isfinite(td_errors).all():
            raise ValueError("Priorities require matching finite TD errors.")

        updated = {}
        for i, error in zip(indices, td_errors):
            if not 0 <= i < len(self):
                raise ValueError("Replay index out of range.")
            updated[i] = max(updated.get(i, 0), abs(float(error)) + 1e-6)
        for i, priority in updated.items():
            self.priorities[i] = priority
