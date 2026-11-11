import argparse
import json
from pathlib import Path

import numpy as np
import torch

from .env import Action, LearnerState, VirtualClassroom
from .model import DQNAgent
from .paths import ROOT, output_path
from .replay.dataset import transition_record


def recommend(agent, state):
    state.validate(agent.env_config)
    action = Action.decode(agent.select_action(state))
    node = agent.constraint_env.target_node(state, action)
    graduation = action.macro == 0 and state.position == agent.env_config.num_courses - 1
    return {"action": action.to_dict(), "target_node": node,
            "target_course": agent.env_config.courses[node],
            "decision": "graduate" if graduation else "teach",
            "courseware_template": None if graduation else (
                f"{agent.env_config.courses[node]}/{action.to_dict()['macro']}/"
                f"{action.to_dict()['micro']['difficulty']}/{action.to_dict()['micro']['style']}"),
            "q_value": float(agent.q_values(state)[action.index]),
            "valid_actions": np.flatnonzero(agent.constraint_env.action_mask(state)).tolist()}


def rollout(agent, seed=2026, profile=0, initial_state=None):
    env = VirtualClassroom(agent.env_config, seed)
    state, _ = env.reset(seed=seed, profile=profile, state=initial_state)
    rows, total = [], 0.0
    while True:
        mask = env.action_mask(state)
        action = agent.select_action(state)
        next_state, reward, terminated, truncated, info = env.step(action)
        rows.append(transition_record(state, action, reward, next_state, terminated, truncated, mask, info))
        total += reward
        state = next_state
        if terminated or truncated:
            break
    return {"seed": seed, "profile": state.profile, "return": total, "length": len(rows),
            "success": terminated, "final_state": state.to_dict(), "transitions": rows}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--checkpoint", default=str(ROOT / "model/checkpoints/dqn.pt"))
    parser.add_argument("--state", help="LearnerState JSON; omitted to sample a new student")
    parser.add_argument("--rollout", action="store_true")
    parser.add_argument("--seed", type=int, default=2026)
    parser.add_argument("--profile", type=int, choices=(0, 1, 2), default=0)
    parser.add_argument("--device", default="cpu")
    parser.add_argument("--output", help="Save recommendation/rollout JSON inside model/dqn")
    args = parser.parse_args()
    torch.set_num_threads(1)
    agent = DQNAgent.load(args.checkpoint, args.device)
    state = LearnerState.from_dict(json.loads(Path(args.state).read_text())) if args.state else None
    if args.rollout:
        result = rollout(agent, args.seed, args.profile, state)
    else:
        if state is None:
            state, _ = VirtualClassroom(agent.env_config).reset(seed=args.seed, profile=args.profile)
        result = recommend(agent, state)
    if args.output:
        output_path(args.output).write_text(json.dumps(result, indent=2) + "\n")
    summary = {k: v for k, v in result.items() if k != "transitions"}
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
