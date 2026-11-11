from dataclasses import asdict, dataclass, replace
from typing import Optional

import numpy as np

MACROS = ("advance", "review", "remediate")
DIFFICULTIES = ("easy", "medium", "hard")
STYLES = ("gentle", "neutral", "strict")
PROFILES = ("balanced", "anxious", "independent")
ACTION_COUNT = 27


@dataclass(frozen=True)
class Action:
    macro: int
    difficulty: int
    style: int

    def __post_init__(self):
        if any(type(v) is not int or not 0 <= v < 3
               for v in (self.macro, self.difficulty, self.style)):
            raise ValueError("Action components must be integers in [0, 2].")

    @property
    def index(self):
        return self.macro * 9 + self.difficulty * 3 + self.style

    @classmethod
    def decode(cls, index):
        if not isinstance(index, (int, np.integer)) or not 0 <= index < ACTION_COUNT:
            raise ValueError("Action index must be an integer in [0, 26].")
        index = int(index)
        return cls(index // 9, (index % 9) // 3, index % 3)

    def to_dict(self):
        return {"index": self.index, "macro": MACROS[self.macro],
                "micro": {"difficulty": DIFFICULTIES[self.difficulty],
                          "style": STYLES[self.style]}}


@dataclass(frozen=True)
class EnvConfig:
    courses: tuple = ("foundations", "concepts", "worked_examples", "application",
                      "integration", "capstone")
    prerequisites: tuple = ((), (0,), (1,), (1, 2), (2, 3), (4,))
    max_steps: int = 40
    mastery_threshold: float = 0.70
    misconception_threshold: float = 0.35
    reward_scale: float = 5.0

    def __post_init__(self):
        if not self.courses or len(set(self.courses)) != len(self.courses):
            raise ValueError("Course names must be nonempty and unique.")
        if len(self.prerequisites) != len(self.courses):
            raise ValueError("Each course needs a prerequisite list.")
        for node, prerequisites in enumerate(self.prerequisites):
            if any(type(p) is not int or not 0 <= p < node for p in prerequisites):
                raise ValueError("Prerequisites must precede their curriculum node.")
        if self.max_steps < 1 or not np.isfinite(self.reward_scale) or self.reward_scale <= 0:
            raise ValueError("max_steps and reward_scale must be positive.")
        if not 0 < self.mastery_threshold < 1 or not 0 < self.misconception_threshold < 1:
            raise ValueError("Curriculum thresholds must lie strictly between 0 and 1.")

    @property
    def num_courses(self):
        return len(self.courses)

    @property
    def continuous_dim(self):
        return 2 * self.num_courses + 5

    def to_dict(self):
        return asdict(self)

    @classmethod
    def from_dict(cls, data):
        return cls(**{**data, "courses": tuple(data["courses"]),
                      "prerequisites": tuple(tuple(p) for p in data["prerequisites"])})


@dataclass(frozen=True)
class LearnerState:
    mastery: tuple
    engagement: float
    fatigue: float
    misconception: tuple
    position: int
    profile: int
    assessment: float
    step: int
    completed: bool = False

    def validate(self, config):
        if len(self.mastery) != config.num_courses or len(self.misconception) != config.num_courses:
            raise ValueError("State vectors must match the curriculum size.")
        values = (*self.mastery, self.engagement, self.fatigue,
                  *self.misconception, self.assessment)
        if not all(np.isfinite(v) and 0 <= v <= 1 for v in values):
            raise ValueError("Continuous state values must be finite and in [0, 1].")
        for value, limit in ((self.position, config.num_courses), (self.profile, len(PROFILES)),
                             (self.step, config.max_steps + 1)):
            if type(value) is not int or not 0 <= value < limit:
                raise ValueError("Invalid position, profile, or step.")
        if type(self.completed) is not bool or (self.completed and self.position != config.num_courses - 1):
            raise ValueError("Invalid curriculum completion flag.")

    def to_dict(self):
        return asdict(self)

    @classmethod
    def from_dict(cls, data):
        return cls(**{**data, "mastery": tuple(data["mastery"]),
                      "misconception": tuple(data["misconception"])})

    def continuous(self, config):
        self.validate(config)
        return np.asarray((*self.mastery, self.engagement, self.fatigue,
                           *self.misconception, self.assessment,
                           1 - self.step / config.max_steps, float(self.completed)), dtype=np.float32)


class VirtualClassroom:


    def __init__(self, config: Optional[EnvConfig] = None, seed=42):
        self.config = config or EnvConfig()
        self.rng = np.random.default_rng(seed)
        self.state = None
        self.finished = False

    def reset(self, seed=None, profile=None, state=None):
        if seed is not None:
            self.rng = np.random.default_rng(seed)
        if state is None:
            profile = int(self.rng.integers(3)) if profile is None else profile
            n = self.config.num_courses
            state = LearnerState(tuple(self.rng.uniform(0.08, 0.38, n)),
                                 float(self.rng.uniform(0.55, 0.90)),
                                 float(self.rng.uniform(0.05, 0.25)),
                                 tuple(self.rng.uniform(0.15, 0.50, n)),
                                 0, profile, 0.25, 0)
        state.validate(self.config)
        if state.step >= self.config.max_steps or state.completed:
            raise ValueError("Cannot reset to a terminal state.")
        self.state, self.finished = state, False
        return state, {"action_mask": self.action_mask(state)}

    def ready(self, state, node):
        return (state.mastery[node] >= self.config.mastery_threshold
                and state.misconception[node] <= self.config.misconception_threshold)

    def target_node(self, state, action):
        if action.macro == 0:
            return min(state.position + 1, self.config.num_courses - 1)
        if action.macro == 1:
            weak = [p for p in self.config.prerequisites[state.position]
                    if not self.ready(state, p)]
            if weak:
                return min(weak, key=lambda p: state.mastery[p])
        return state.position

    def action_mask(self, state=None, terminal=False):
        state = state or self.state
        if state is None:
            raise RuntimeError("Call reset before requesting actions.")
        state.validate(self.config)
        mask = np.zeros(ACTION_COUNT, dtype=bool)
        if terminal or state.completed or state.step >= self.config.max_steps:
            return mask
        target = min(state.position + 1, self.config.num_courses - 1)
        advance = (self.ready(state, state.position)
                   and all(self.ready(state, p) for p in self.config.prerequisites[target]))
        for index in range(ACTION_COUNT):
            a = Action.decode(index)
            if a.macro == 0 and not advance:
                continue
            if a.macro == 2 and state.misconception[state.position] < 0.12:
                continue
            node = self.target_node(state, a)
            if a.difficulty == 2 and (state.mastery[node] < 0.45 or state.fatigue > 0.65):
                continue
            if a.difficulty == 1 and state.fatigue > 0.85:
                continue
            if a.style == 2 and (state.engagement < 0.40 or state.fatigue > 0.65
                                 or state.profile == 1):
                continue
            mask[index] = True
        return mask

    def step(self, action_index):
        if self.state is None or self.finished:
            raise RuntimeError("Call reset before stepping a new episode.")
        action = Action.decode(action_index)
        if not self.action_mask()[action.index]:
            raise ValueError("Action violates curriculum or pedagogical constraints.")
        s, c = self.state, self.config
        node = self.target_node(s, action)
        graduation = action.macro == 0 and s.position == c.num_courses - 1
        if graduation:
            next_state = replace(s, step=s.step + 1, completed=True)
            raw_reward = 5.0
        else:
            mastery, misconceptions = np.array(s.mastery), np.array(s.misconception)
            preferred_style = (1, 0, 2)[s.profile]
            style_fit = 1.0 if action.style == preferred_style else 0.82
            preferred_difficulty = 0 if mastery[node] < 0.35 else (1 if mastery[node] < 0.75 else 2)
            difficulty_fit = max(0.45, 1 - 0.25 * abs(action.difficulty - preferred_difficulty))
            learning_rate = (0.27, 0.23, 0.31)[s.profile]
            gain = (learning_rate * style_fit * difficulty_fit * (0.5 + 0.5 * s.engagement)
                    * (1 - 0.65 * s.fatigue) * (1 - mastery[node])
                    * (1 - 0.35 * misconceptions[node]))
            if action.macro == 2:
                gain *= 0.85
            mastery[node] = np.clip(mastery[node] + gain + self.rng.normal(0, 0.012), 0, 1)
            correction = (0.04, 0.05, 0.14)[action.macro] * style_fit
            misconceptions[node] = np.clip(misconceptions[node] - correction
                                           + self.rng.normal(0, 0.01), 0, 1)
            fatigue = float(np.clip(s.fatigue + (-0.04, 0.025, 0.065)[action.difficulty]
                                    + (-0.025, 0, 0.025)[action.style], 0, 1))
            engagement = float(np.clip(s.engagement + (0.025 if style_fit == 1 else -0.025)
                                       + (0.01 if difficulty_fit == 1 else -0.015)
                                       - 0.015 * fatigue + self.rng.normal(0, 0.01), 0, 1))
            assessment = float(np.clip(mastery[node] * (1 - 0.4 * misconceptions[node])
                                       + self.rng.normal(0, 0.025), 0, 1))
            next_state = LearnerState(tuple(mastery), engagement, fatigue, tuple(misconceptions),
                                      node if action.macro == 0 else s.position, s.profile,
                                      assessment, s.step + 1)
            raw_reward = (8 * (mastery[node] - s.mastery[node])
                          + 2 * (s.misconception[node] - misconceptions[node])
                          + 0.5 * (engagement - s.engagement)
                          - 0.5 * max(0, fatigue - s.fatigue) - 0.08
                          + (0.8 if action.macro == 0 else 0))
        terminated = bool(graduation)
        truncated = bool(next_state.step >= c.max_steps and not terminated)
        if truncated:
            raw_reward -= 1.0
        reward = float(np.clip(raw_reward / c.reward_scale, -1, 1))
        next_state.validate(c)
        self.state, self.finished = next_state, terminated or truncated
        info = {"action_mask": self.action_mask(next_state, terminal=self.finished),
                "target_node": node, "target_course": c.courses[node],
                "raw_reward": float(raw_reward), "success": terminated,
                "termination_reason": "graduated" if terminated else ("time_limit" if truncated else None)}
        return next_state, reward, terminated, truncated, info

    def heuristic_action(self, state=None):

        s = state or self.state
        valid = np.flatnonzero(self.action_mask(s))
        if not len(valid):
            raise ValueError("No actions available for a terminal state.")
        macro = 0 if any(Action.decode(i).macro == 0 for i in valid) else (
            2 if s.misconception[s.position] > 0.28 else 1)
        node = self.target_node(s, Action(macro, 0, 0))
        difficulty = 0 if s.mastery[node] < 0.35 or s.fatigue > 0.45 else 1
        style = (1, 0, 2)[s.profile]
        def score(index):
            a = Action.decode(index)
            return 5 * (a.macro == macro) + 2 * (a.difficulty == difficulty) + (a.style == style)
        return int(max(valid, key=score))
