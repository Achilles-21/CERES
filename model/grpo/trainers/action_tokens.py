def teacher_action_ids(input_ids, labels, steps, tokenizer):
    'Align complete teacher-token runs to actions; reject partial or ambiguous matches.\nControl-only endings inherit their preceding teaching action.'


    if len(input_ids) != len(labels):
        raise ValueError("Token/label length mismatch")
    runs = []
    for i, label in enumerate(labels):
        if label != -100:
            if label != input_ids[i]:
                raise ValueError("Teacher label does not match input token")
            if not runs or runs[-1][-1] != i - 1:
                runs.append([])
            runs[-1].append(i)
    if len(runs) != len(steps):
        raise ValueError(f"Teacher/action alignment failed: {len(runs)} label runs vs {len(steps)} actions")
    owners = [-1] * len(input_ids)
    special = set(tokenizer.all_special_ids)
    for l, (positions, step) in enumerate(zip(runs, steps)):
        body = tokenizer.encode(step["teacher"], add_special_tokens=False)
        observed = [input_ids[p] for p in positions]
        starts = [i for i in range(len(observed) - len(body) + 1)
                  if observed[i:i + len(body)] == body]
        if not body or not starts:
            raise ValueError(f"Teacher body tokens missing at action {l}; no partial alignment allowed")
        def harmless(ids):
            return not tokenizer.decode([i for i in ids if i not in special]).strip()
        starts = [i for i in starts if harmless(observed[:i]) and harmless(observed[i + len(body):])]
        if len(starts) != 1:
            raise ValueError(f"Unexpected teacher prefix/suffix at action {l}")
        owner = step.get("credit_step", l)
        if not 0 <= owner < len(steps):
            raise ValueError("Invalid action ownership")
        for p in positions:
            owners[p] = owner
    return owners
