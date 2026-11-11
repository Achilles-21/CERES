import numpy as np

from .env import VirtualClassroom


def evaluate(config, episodes=20, seed=10000, agent=None, policy="dqn"):
    if episodes < 1 or policy not in ("dqn", "random", "heuristic"):
        raise ValueError("Positive episode count and a supported policy required.")
    if policy == "dqn" and agent is None:
        raise ValueError("DQN evaluation requires an agent.")
    env, rng = VirtualClassroom(config, seed), np.random.default_rng(seed)
    returns, lengths, mastery, successes = [], [], [], 0
    for episode in range(episodes):
        state, _ = env.reset(seed=seed + episode, profile=episode % 3)
        total = 0.0
        while True:
            if policy == "dqn":
                action = agent.select_action(state)
            elif policy == "heuristic":
                action = env.heuristic_action(state)
            else:
                action = int(rng.choice(np.flatnonzero(env.action_mask(state))))
            state, reward, terminated, truncated, _ = env.step(action)
            total += reward
            if terminated or truncated:
                successes += int(terminated)
                returns.append(total)
                lengths.append(state.step)
                mastery.append(float(np.mean(state.mastery)))
                break
    return {"policy": policy, "episodes": episodes, "seed": seed,
            "mean_return": float(np.mean(returns)), "success_rate": successes / episodes,
            "mean_length": float(np.mean(lengths)), "mean_mastery": float(np.mean(mastery))}
