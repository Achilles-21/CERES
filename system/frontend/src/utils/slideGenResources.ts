export interface SlideResourceImage {
  image_id: string | number
  position?: string
  query?: string
}

const RESOURCE_READY_STATUS_KEYWORDS = [
  '配图匹配完成',
  'PPT生成完成',
  '配图已完成',
  'Images matched',
  'PPT generation complete',
  'Images are ready',
  'Imej dipadankan',
  'Penjanaan PPT selesai',
  'Imej sudah tersedia',
  'चित्र मिल गए',
  'PPT जनरेशन पूरी',
  'चित्र तैयार'
]

export function isResourceReadyStatus(content = ''): boolean {
  return RESOURCE_READY_STATUS_KEYWORDS.some(keyword => content.includes(keyword))
}

export function isFinalResourceStatus(content = ''): boolean {
  return [
    'PPT生成完成',
    'PPT生成失败',
    '配图已完成',
    'PPT generation complete',
    'PPT generation failed',
    'Images are ready',
    'Penjanaan PPT selesai',
    'Penjanaan PPT gagal',
    'Imej sudah tersedia',
    'PPT जनरेशन पूरी',
    'PPT जनरेशन विफल',
    'चित्र तैयार'
  ].some(keyword => content.includes(keyword))
}

export function normalizeSlideImages(data: unknown): SlideResourceImage[] {
  if (!data || typeof data !== 'object' || !Array.isArray((data as { images?: unknown }).images)) return []

  return ((data as { images: unknown[] }).images.filter((item): item is SlideResourceImage => {
    return Boolean(item && typeof item === 'object' && (item as SlideResourceImage).image_id !== undefined && (item as SlideResourceImage).image_id !== null)
  }))
}
