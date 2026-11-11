export const SLIDEGEN_AVATAR_BASE = '/slidegen/avatars'

const agentAvatarAliases: Record<string, string> = {
  designer: 'DesignerAgent',
  reviewer: 'ReviewerAgent',
  planner: 'PlannerAgent',
  system: 'System'
}

export function slideGenUserAvatar(index: number): string {
  return `${SLIDEGEN_AVATAR_BASE}/User${index}.svg`
}

export function slideGenAgentAvatar(name: string): string {
  const normalizedName = agentAvatarAliases[name.toLowerCase()] || name
  return `${SLIDEGEN_AVATAR_BASE}/${normalizedName}.svg`
}
