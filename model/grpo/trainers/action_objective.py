'Action likelihood ratios with teacher-token masking and a global action-count denominator.'
import torch


def action_grpo_loss(logps, old_logps, ref_logps, advantages, action_ids, mask,
                     epsilon_low, epsilon_high, beta):
    if logps.shape != advantages.shape or logps.shape != action_ids.shape or logps.shape != mask.shape:
        raise ValueError("Log probabilities, advantages, action ids and mask must have identical shapes")
    # Mask student and padding tokens before summation and exponentiation.
    active = mask.bool() & (action_ids >= 0)

    ratio_logs, action_advs, kls = [], [], []
    for row in range(logps.shape[0]):
        for owner in torch.unique(action_ids[row][active[row]]):
            selected = active[row] & (action_ids[row] == owner)
            values = advantages[row][selected]
            if not torch.allclose(values, values[0].expand_as(values)):
                raise ValueError("One action has inconsistent token advantages")
            ratio_logs.append((logps[row][selected] - old_logps[row][selected]).sum())
            action_advs.append(values[0])
            if beta:
                diff = ref_logps[row][selected] - logps[row][selected]
                kls.append((torch.exp(diff) - diff - 1).sum())
            else:
                kls.append(logps[row][selected].sum() * 0)
    if ratio_logs:
        ratio = torch.exp(torch.stack(ratio_logs))
        adv = torch.stack(action_advs)
        policy = -torch.minimum(ratio * adv,
                                ratio.clamp(1 - epsilon_low, 1 + epsilon_high) * adv)
        kl = torch.stack(kls)
        total = (policy + beta * kl).sum()
    else:
        total = logps[active].sum() * 0
        policy = kl = logps.new_zeros(1)
    count = logps.new_tensor(float(len(ratio_logs)))
    world = 1
    if torch.distributed.is_available() and torch.distributed.is_initialized():
        world = torch.distributed.get_world_size()
        torch.distributed.all_reduce(count)
    # Compensate for DDP gradient averaging using the global action count.
    loss = total * world / count.clamp(min=1)
    if not torch.isfinite(loss):
        raise FloatingPointError("Nonfinite CERES action objective")
    return loss, {"policy": policy.detach().mean(), "kl": kl.detach().mean(),
                  "actions_local": len(ratio_logs)}
