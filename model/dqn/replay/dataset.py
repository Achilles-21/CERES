import json
from pathlib import Path

import numpy as np

from ..env import ACTION_COUNT, Action, EnvConfig, LearnerState, VirtualClassroom
from .buffer import Transition


def transition_record(state, action, reward, next_state, terminated, truncated, mask, info):
    decoded = Action.decode(action)
    node = info["target_node"]
    successful = bool(reward > 0 and (
        next_state.mastery[node] - state.mastery[node] >= 0.02
        or state.misconception[node] - next_state.misconception[node] >= 0.03
        or decoded.macro == 0))
    return {"state": state.to_dict(), "action": decoded.to_dict(), "reward": reward,
            "next_state": next_state.to_dict(), "terminated": terminated,
            "truncated": truncated, "done": terminated or truncated,
            "valid_actions": np.flatnonzero(mask).tolist(),
            "next_valid_actions": np.flatnonzero(info["action_mask"]).tolist(),
            "successful": successful, "target_course": info["target_course"]}


def parse_transition(row, env):
    state = LearnerState.from_dict(row["state"])
    next_state = LearnerState.from_dict(row["next_state"])
    state.validate(env.config)
    next_state.validate(env.config)
    action = Action.decode(row["action"]["index"])
    if row["action"] != action.to_dict():
        raise ValueError("Action index and hierarchical labels disagree.")
    for key in ("terminated", "truncated", "done", "successful"):
        if type(row[key]) is not bool:
            raise ValueError(f"{key} must be a boolean.")
    terminated, truncated = row["terminated"], row["truncated"]
    done = terminated or truncated
    if terminated and truncated or row["done"] != done:
        raise ValueError("Inconsistent terminal flags.")
    if next_state.step != state.step + 1 or state.profile != next_state.profile:
        raise ValueError("Invalid MDP state progression.")
    node = env.target_node(state, action)
    expected_position = node if action.macro == 0 else state.position
    graduation = action.macro == 0 and state.position == env.config.num_courses - 1
    if (next_state.position != expected_position or terminated != graduation
            or next_state.completed != terminated
            or truncated != (next_state.step == env.config.max_steps and not terminated)):
        raise ValueError("Invalid curriculum progression or episode boundary.")
    if row["target_course"] != env.config.courses[node]:
        raise ValueError("Incorrect target course.")
    mask = env.action_mask(state)
    next_mask = env.action_mask(next_state, terminal=done)
    if (row["valid_actions"] != np.flatnonzero(mask).tolist()
            or row["next_valid_actions"] != np.flatnonzero(next_mask).tolist()):
        raise ValueError("Stored masks disagree with environment constraints.")
    if not mask[action.index] or not np.isfinite(row["reward"]) or not -1 <= row["reward"] <= 1:
        raise ValueError("Invalid action or reward in replay.")
    return Transition(state, action.index, float(row["reward"]), next_state,
                      terminated, truncated, tuple(mask), tuple(next_mask), row["successful"])


def load_dataset(path):
    path = Path(path)
    manifest = json.loads(path.with_suffix(".meta.json").read_text())
    if manifest["schema_version"] != 1:
        raise ValueError("Unsupported trajectory schema.")
    config = EnvConfig.from_dict(manifest["env_config"])
    env = VirtualClassroom(config)
    transitions, episodes, identifiers = [], 0, set()
    with path.open() as f:
        for line_no, line in enumerate(f, 1):
            try:
                trajectory = json.loads(line)
                if trajectory["trajectory_id"] in identifiers:
                    raise ValueError("Duplicate trajectory ID.")
                identifiers.add(trajectory["trajectory_id"])
                rows = trajectory["transitions"]
                if not rows:
                    raise ValueError("Empty trajectory.")
                previous = None
                for index, row in enumerate(rows):
                    transition = parse_transition(row, env)
                    if previous is not None and transition.state != previous:
                        raise ValueError("Disconnected trajectory states.")
                    if index == 0 and transition.state.step != 0:
                        raise ValueError("Trajectory must begin at step zero.")
                    if transition.done != (index == len(rows) - 1):
                        raise ValueError("Episode must end exactly once at its last transition.")
                    transitions.append(transition)
                    previous = transition.next_state
                if trajectory["profile"] != rows[0]["state"]["profile"]:
                    raise ValueError("Trajectory profile mismatch.")
                episodes += 1
            except (KeyError, TypeError, ValueError) as exc:
                raise ValueError(f"Invalid trajectory at {path}:{line_no}: {exc}") from exc
    if episodes != manifest["num_trajectories"] or len(transitions) != manifest["num_transitions"]:
        raise ValueError("Manifest counts disagree with trajectory data.")
    return config, transitions, manifest
