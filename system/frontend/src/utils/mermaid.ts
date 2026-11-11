import mermaid from 'mermaid'
import { marked, type Renderer } from 'marked'

mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
  fontFamily: 'inherit',
})

const renderer: Partial<Renderer> = {
  code(this: Renderer, token: { text: string; lang?: string }) {
    const lang = (token.lang || '').trim()
    const code = token.text ?? ''
    if (lang === 'mermaid') {
      const escaped = code
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
      return `<div class="mermaid-placeholder" data-mermaid-code="${escaped}"></div>`
    }
    return false as any
  },
}

marked.use({ renderer })

let idCounter = 0

const SPECIAL_CHARS = /[\(\)\[\]\{\}\|;]/
const NODE_OPENERS: Array<[string, string]> = [
  ['[[', ']]'],
  ['[(', ')]'],
  ['((', '))'],
  ['{{', '}}'],
  ['[/', '/]'],
  ['[\\', '\\]'],
  ['[', ']'],
  ['{', '}'],
  ['(', ')'],
]

function needsQuoting(text: string): boolean {
  const t = text.trim()
  if (!t) return false
  if (t.startsWith('"') || t.startsWith("'")) return false
  if (t.includes('"') || t.includes("'")) return false
  return SPECIAL_CHARS.test(t)
}

function sanitizeLine(line: string): string {
  let result = ''
  let i = 0
  const n = line.length
  while (i < n) {
    let matched = false
    for (const [open, close] of NODE_OPENERS) {
      if (!line.startsWith(open, i)) continue
      const closeStart = i + open.length
      const closeIdx = line.indexOf(close, closeStart)
      if (closeIdx === -1) {
        result += line[i]
        i += 1
        matched = true
        break
      }
      const text = line.slice(closeStart, closeIdx)
      if (needsQuoting(text)) {
        result += `${open}"${text}"${close}`
      } else {
        result += line.slice(i, closeIdx + close.length)
      }
      i = closeIdx + close.length
      matched = true
      break
    }
    if (!matched) {
      result += line[i]
      i += 1
    }
  }
  return result
}

function sanitizeMermaidCode(code: string): string {
  return code.split('\n').map(sanitizeLine).join('\n')
}

export async function renderMermaidIn(el: HTMLElement | null | undefined): Promise<void> {
  if (!el) return
  const blocks = el.querySelectorAll<HTMLElement>('.mermaid-placeholder:not([data-rendered])')
  if (!blocks.length) return

  for (const block of blocks) {
    const rawCode = (block.dataset.mermaidCode || '')
      .replace(/&amp;/g, '&')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&quot;/g, '"')

    if (!rawCode.trim()) {
      block.setAttribute('data-rendered', 'true')
      continue
    }

    const code = sanitizeMermaidCode(rawCode)
    const id = `mmd-${Date.now()}-${idCounter++}`
    try {
      const { svg } = await mermaid.render(id, code)
      const wrapper = document.createElement('div')
      wrapper.className = 'mermaid-container'
      wrapper.innerHTML = svg
      wrapper.setAttribute('role', 'button')
      wrapper.setAttribute('tabindex', '0')
      wrapper.setAttribute('title', '点击放大')
      wrapper.setAttribute('aria-label', '放大图表')
      wrapper.addEventListener('click', () => openMermaidLightbox(svg))
      wrapper.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          openMermaidLightbox(svg)
        }
      })
      block.replaceWith(wrapper)
    } catch (err) {
      console.warn('[Mermaid] render error:', err)
      // Display the original source when Mermaid rendering fails.
      const pre = document.createElement('pre')
      pre.className = 'mermaid-fallback'
      const codeEl = document.createElement('code')
      codeEl.textContent = code
      pre.appendChild(codeEl)
      block.replaceWith(pre)
    }
  }
}

export function parseMarkdown(content: string): string {
  return marked.parse(content || '', { breaks: true, gfm: true }) as string
}

function getSvgBaseWidth(svg: SVGSVGElement): number {
  const viewBox = svg.getAttribute('viewBox')
  if (viewBox) {
    const parts = viewBox.trim().split(/\s+/).map(Number)
    if (parts.length >= 4 && Number.isFinite(parts[2]) && parts[2] > 0) return parts[2]
  }
  const width = svg.getAttribute('width')
  if (width) {
    const parsed = parseFloat(width)
    if (Number.isFinite(parsed) && parsed > 0) return parsed
  }
  return 800
}

function openMermaidLightbox(svg: string): void {
  document.querySelector('.mermaid-lightbox')?.remove()

  const overlay = document.createElement('div')
  overlay.className = 'mermaid-lightbox'

  const backdrop = document.createElement('div')
  backdrop.className = 'mermaid-lightbox-backdrop'

  const closeBtn = document.createElement('button')
  closeBtn.type = 'button'
  closeBtn.className = 'mermaid-lightbox-close'
  closeBtn.setAttribute('aria-label', '关闭')
  closeBtn.textContent = '×'

  const toolbar = document.createElement('div')
  toolbar.className = 'mermaid-lightbox-toolbar'

  const viewport = document.createElement('div')
  viewport.className = 'mermaid-lightbox-viewport'

  const stage = document.createElement('div')
  stage.className = 'mermaid-lightbox-stage'
  stage.innerHTML = svg

  viewport.appendChild(stage)
  overlay.append(backdrop, toolbar, closeBtn, viewport)
  document.body.appendChild(overlay)

  const svgEl = stage.querySelector('svg')
  const baseWidth = svgEl ? getSvgBaseWidth(svgEl) : 800
  let scale = 1

  const applyScale = () => {
    if (!svgEl) return
    svgEl.style.width = `${baseWidth * scale}px`
    svgEl.style.height = 'auto'
    svgEl.style.maxWidth = 'none'
  }

  const zoomBy = (factor: number) => {
    scale = Math.min(8, Math.max(0.05, scale * factor))
    applyScale()
  }

  const addBtn = (label: string, fn: () => void) => {
    const btn = document.createElement('button')
    btn.type = 'button'
    btn.textContent = label
    btn.addEventListener('click', fn)
    toolbar.appendChild(btn)
  }
  addBtn('+', () => zoomBy(1.25))
  addBtn('−', () => zoomBy(0.8))
  addBtn('1:1', () => {
    scale = 1
    applyScale()
  })

  const close = () => {
    document.removeEventListener('keydown', onKey)
    document.body.style.overflow = ''
    overlay.remove()
  }
  const onKey = (e: KeyboardEvent) => {
    if (e.key === 'Escape') close()
  }

  closeBtn.addEventListener('click', close)
  backdrop.addEventListener('click', close)
  document.addEventListener('keydown', onKey)
  document.body.style.overflow = 'hidden'

  const available = viewport.clientWidth - 128
  const fitScale = available > 0 ? available / baseWidth : 1
  scale = Math.min(8, Math.max(0.05, fitScale))
  applyScale()
}
