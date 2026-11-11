const API_PREFIX = import.meta.env.VITE_SLIDEGEN_API || '/slidegen-api'
const WS_PREFIX = '/slidegen-ws'

export function getSlideGenApiBase(): string {
  return API_PREFIX
}

export function getSlideGenWsUrl(userId: string, locale = 'zhcn'): string {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const params = new URLSearchParams({ locale })
  return `${proto}//${window.location.host}${WS_PREFIX}/ws/${userId}?${params.toString()}`
}

export function slideResourceUrl(userId: string, slideId: string, suffix: string): string {
  return `${API_PREFIX}/slides/${userId}/${slideId}${suffix}`
}
