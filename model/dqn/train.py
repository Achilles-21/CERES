import argparse
from dataclasses import asdict
import json
import random

import numpy as np
import torch

from .env import VirtualClassroom
from .evaluate import evaluate
from .model import AgentConfig, DQNAgent
from .paths import ROOT, output_path
from .replay.buffer import PrioritizedReplay, Transition
from .replay.dataset import load_dataset, transition_record
from .replay.generate import generate


def epsilon_at(step, decay_steps, start=1.0, end=0.05):
    return start + (end - start) * min(1.0, step / max(1, decay_steps))


def train(args):
    if (min(args.bc_steps, args.offline_steps, args.episodes) < 0
            or args.eval_episodes < 1 or args.capacity < 2 or args.threads < 1
            or args.epsilon_decay < 1 or not 0 < args.offline_fraction < 1):
        raise ValueError("Invalid training schedule, capacity, or replay ratio.")
    random.seed(args.seed)
    np.random.seed(args.seed)
    torch.manual_seed(args.seed)
    torch.set_num_threads(args.threads)
    dataset = output_path(args.dataset)
    if not dataset.exists():
        generate(dataset, seed=args.seed)
    config, transitions, manifest = load_dataset(dataset)
    agent_config = AgentConfig(batch_size=args.batch_size, learning_rate=args.learning_rate,
                               target_interval=args.target_interval)
    agent = DQNAgent(config, agent_config, args.device, args.seed)
    demonstrations = [t for t in transitions if t.successful]
    metrics = {"seed": args.seed, "dataset": str(dataset.relative_to(ROOT)),
               "dataset_manifest": manifest, "agent_config": asdict(agent_config),
               "training_config": {"bc_steps": args.bc_steps, "offline_steps": args.offline_steps,
                                   "episodes": args.episodes, "capacity": args.capacity,
                                   "alpha": args.alpha, "beta": args.beta,
                                   "offline_fraction": args.offline_fraction,
                                   "epsilon_decay": args.epsilon_decay},
               "successful_demonstrations": len(demonstrations), "episodes": []}
    bc_losses = agent.behavior_clone(demonstrations, args.bc_steps)
    metrics["bc"] = {"steps": len(bc_losses), "initial_loss": bc_losses[0] if bc_losses else None,
                     "final_loss": bc_losses[-1] if bc_losses else None}
    print(f"Behavior cloning: {len(demonstrations)} synthetic demonstrations, {len(bc_losses)} updates", flush=True)

    filtered = PrioritizedReplay(args.capacity, args.alpha, args.seed)
    for transition in demonstrations:
        filtered.add(transition)
    if args.offline_steps and not len(filtered):
        raise ValueError("Filtered offline TD needs successful replay transitions.")
    offline_losses = []
    for _ in range(args.offline_steps):
        offline_losses.append(agent.update(filtered, args.beta)["loss"])
    metrics["offline_td"] = {"steps": args.offline_steps,
                             "mean_loss": float(np.mean(offline_losses)) if offline_losses else None}
    print(f"Offline TD: {args.offline_steps} updates", flush=True)

    offline_capacity = min(args.capacity - 1, max(1, round(args.capacity * args.offline_fraction)))
    replay = PrioritizedReplay(args.capacity, args.alpha, args.seed + 1,
                               source_limits={"offline": offline_capacity,
                                              "online": args.capacity - offline_capacity})
    for transition in transitions:
        replay.add(transition)
    env, total_steps = VirtualClassroom(config, args.seed + 1000), 0
    for episode in range(args.episodes):
        state, _ = env.reset(seed=args.seed + 1000 + episode, profile=episode % 3)
        total_reward, losses = 0.0, []
        while True:
            epsilon = epsilon_at(total_steps, args.epsilon_decay)
            mask = env.action_mask(state)
            action = agent.select_action(state, epsilon)
            next_state, reward, terminated, truncated, info = env.step(action)
            record = transition_record(state, action, reward, next_state, terminated, truncated, mask, info)
            replay.add(Transition(state, action, reward, next_state, terminated, truncated,
                                  tuple(mask), tuple(info["action_mask"]), record["successful"]), "online")
            losses.append(agent.update(replay, args.beta, args.offline_fraction)["loss"])
            total_steps += 1
            total_reward += reward
            state = next_state
            if terminated or truncated:
                break
        result = {"episode": episode + 1, "return": total_reward, "length": state.step,
                  "success": terminated, "epsilon": epsilon, "mean_loss": float(np.mean(losses))}
        metrics["episodes"].append(result)
        if (episode + 1) % 10 == 0 or episode + 1 == args.episodes:
            print(f"Online {episode + 1}/{args.episodes}: return={total_reward:.3f}, "
                  f"epsilon={epsilon:.3f}, updates={agent.updates}", flush=True)
    metrics["online_steps"] = total_steps
    metrics["td_updates"] = agent.updates
    metrics["replay_size"] = len(replay)
    metrics["replay_sources"] = {source: replay.sources.count(source) for source in ("offline", "online")}

    metrics["evaluation"] = [evaluate(config, args.eval_episodes, args.eval_seed, agent, policy)
                             for policy in ("random", "heuristic", "dqn")]
    checkpoint = output_path(args.checkpoint)
    agent.save(checkpoint, {"seed": args.seed, "synthetic": True,
                            "dataset": str(dataset.relative_to(ROOT))})
    metrics_path = output_path(args.metrics)
    metrics_path.write_text(json.dumps(metrics, indent=2) + "\n")
    print(json.dumps({"checkpoint": str(checkpoint), "metrics": str(metrics_path),
                      "evaluation": metrics["evaluation"]}, indent=2))
    return metrics


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dataset", default=str(ROOT / "replay/trajectories.jsonl"))
    parser.add_argument("--checkpoint", default=str(ROOT / "model/checkpoints/dqn.pt"))
    parser.add_argument("--metrics", default=str(ROOT / "model/checkpoints/training_metrics.json"))
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--device", default="cpu")
    parser.add_argument("--threads", type=int, default=1)
    parser.add_argument("--bc-steps", type=int, default=500)
    parser.add_argument("--offline-steps", type=int, default=500)
    parser.add_argument("--episodes", type=int, default=300)
    parser.add_argument("--eval-episodes", type=int, default=20)
    parser.add_argument("--eval-seed", type=int, default=10000)
    parser.add_argument("--batch-size", type=int, default=32)
    parser.add_argument("--capacity", type=int, default=5000)
    parser.add_argument("--learning-rate", type=float, default=0.0003)
    parser.add_argument("--target-interval", type=int, default=500)
    parser.add_argument("--alpha", type=float, default=0.6)
    parser.add_argument("--beta", type=float, default=0.4)
    parser.add_argument("--offline-fraction", type=float, default=0.5)
    parser.add_argument("--epsilon-decay", type=int, default=4000)
    train(parser.parse_args())


if __name__ == "__main__":
    main()
