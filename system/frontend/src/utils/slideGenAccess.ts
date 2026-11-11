/** Role 3 identifies student accounts, which cannot access slide generation. */
export function canUseSlideGen(userInfo: Record<string, unknown> | null | undefined): boolean {
  return String(userInfo?.roleId ?? '') !== '3'
}
