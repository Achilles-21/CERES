import argparse
import json

import numpy as np

from ..env import EnvConfig, VirtualClassroom
from ..paths import ROOT, output_path
from .dataset import transition_record


def generate(path=ROOT / "replay/trajectories.jsonl", episodes=100, seed=42,
             exploration=0.25, config=None):
    if episodes < 1 or not 0 <= exploration <= 1:
        raise ValueError("Positive episode count and exploration in [0, 1] required.")
    config = config or EnvConfig()
    path = output_path(path)
    env, rng = VirtualClassroom(config, seed), np.random.default_rng(seed)
    returns, lengths, successes, count = [], [], 0, 0
    with path.open("w") as f:
        for episode in range(episodes):
            episode_seed = seed + episode
            state, _ = env.reset(seed=episode_seed, profile=episode % 3)
            rows, total = [], 0.0
            while True:
                mask = env.action_mask(state)
                action = int(rng.choice(np.flatnonzero(mask))) if rng.random() < exploration else env.heuristic_action(state)
                next_state, reward, terminated, truncated, info = env.step(action)
                rows.append(transition_record(state, action, reward, next_state,
                                              terminated, truncated, mask, info))
                total += reward
                state = next_state
                if terminated or truncated:
                    successes += int(terminated)
                    break
            f.write(json.dumps({"trajectory_id": f"virtual-{episode:04d}",
                                "seed": episode_seed, "profile": episode % 3,
                                "transitions": rows}, ensure_ascii=False) + "\n")
            returns.append(total)
            lengths.append(len(rows))
            count += len(rows)
    manifest = {"schema_version": 1, "source": "synthetic_virtual_classroom",
                "teacher": "masked heuristic with random exploration; not validated Stage 2 data",
                "seed": seed, "exploration": exploration, "env_config": config.to_dict(),
                "num_trajectories": episodes, "num_transitions": count,
                "graduated_trajectories": successes, "mean_return": float(np.mean(returns)),
                "mean_length": float(np.mean(lengths))}
    output_path(path.with_suffix(".meta.json")).write_text(json.dumps(manifest, indent=2) + "\n")
    return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--episodes", type=int, default=100)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--exploration", type=float, default=0.25)
    parser.add_argument("--output", default=str(ROOT / "replay/trajectories.jsonl"))
    args = parser.parse_args()
    print(json.dumps(generate(args.output, args.episodes, args.seed, args.exploration), indent=2))


if __name__ == "__main__":
    main()
