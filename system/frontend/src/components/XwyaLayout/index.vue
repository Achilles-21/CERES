<script lang="ts" setup>
import useChart from '@/hooks/useChart'
import i18n from '@/lang'

const props = defineProps<{ disable?: boolean; hideChatHeader?: boolean; chatAssistantTitle?: string; hideAssistantDescription?: boolean; chatAssistantRoleLabel?: string; resizable?: boolean }>()
const { msgList, onEnter, clearMessages, modelType } = useChart()
const flag = ref(false)
const assistantCollapsed = ref(false)
const chatAssistantTitle = computed(() => props.chatAssistantTitle || i18n.global.t('chat.assistant'))
const assistantTitle = computed(() => (flag.value ? i18n.global.t('pastlist.notes') : chatAssistantTitle.value))
const assistantDescription = computed(() => (flag.value ? i18n.global.t('system.pleaseEnterContent') : i18n.global.t('chat.learningCopilot')))
const assistantIcon = computed(() => (flag.value ? 'solar--notes-bold' : 'solar--chat-round-line-bold'))

const changeNote = () => {
  flag.value = !flag.value
  assistantCollapsed.value = false
}
const toggleAssistant = () => {
  assistantCollapsed.value = !assistantCollapsed.value
}
const onMessage = () => {
  window.$msg.error(i18n.global.t('userPage.notAccess'))
}

const layoutRef = ref<HTMLElement | null>(null)
const mainRatio = ref(0.6)  
const isDragging = ref(false)
const isMobile = ref(false)
const MIN_RATIO = 0.2
const MAX_RATIO = 0.8
const DEFAULT_RATIO = 0.6
const MOBILE_QUERY = '(max-width: 900px)'
let rafId = 0
let pendingClientX = 0
let mobileQuery: MediaQueryList | null = null

const dividerVisible = computed(() => Boolean(props.resizable) && !assistantCollapsed.value && !isMobile.value)

const layoutStyle = computed(() => {
  if (!dividerVisible.value) return undefined
  const r = mainRatio.value
  return {
    gridTemplateColumns: `minmax(0, ${r}fr) var(--divider-size) minmax(0, ${1 - r}fr)`,
    gap: '0px'
  }
})

const updateRatio = () => {
  rafId = 0
  const el = layoutRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  if (rect.width <= 0) return
  const ratio = (pendingClientX - rect.left) / rect.width
  mainRatio.value = Math.min(MAX_RATIO, Math.max(MIN_RATIO, ratio))
}

const onDividerPointerDown = (e: PointerEvent) => {
  if (!dividerVisible.value || e.button !== 0) return
  e.preventDefault()
  isDragging.value = true
  document.body.classList.add('xwya-resizing')
  ;(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId)
}

const onDividerPointerMove = (e: PointerEvent) => {
  if (!isDragging.value) return
  pendingClientX = e.clientX
  if (!rafId) rafId = requestAnimationFrame(updateRatio)
}

const stopDrag = (e: PointerEvent) => {
  if (!isDragging.value) return
  isDragging.value = false
  document.body.classList.remove('xwya-resizing')
  const target = e.currentTarget as HTMLElement
  if (target.hasPointerCapture(e.pointerId)) target.releasePointerCapture(e.pointerId)
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = 0
  }
}

const onDividerKeyDown = (e: KeyboardEvent) => {
  if (!dividerVisible.value) return
  const step = 0.02
  if (e.key === 'ArrowLeft') {
    mainRatio.value = Math.max(MIN_RATIO, mainRatio.value - step)
    e.preventDefault()
  } else if (e.key === 'ArrowRight') {
    mainRatio.value = Math.min(MAX_RATIO, mainRatio.value + step)
    e.preventDefault()
  }
}

const resetRatio = () => {
  mainRatio.value = DEFAULT_RATIO
}

const handleMediaChange = (e: MediaQueryListEvent | MediaQueryList) => {
  isMobile.value = e.matches
}

onMounted(() => {
  mobileQuery = window.matchMedia(MOBILE_QUERY)
  isMobile.value = mobileQuery.matches
  if (typeof mobileQuery.addEventListener === 'function') {
    mobileQuery.addEventListener('change', handleMediaChange)
  } else {
    mobileQuery.addListener(handleMediaChange)
  }
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
  document.body.classList.remove('xwya-resizing')
  if (mobileQuery) {
    if (typeof mobileQuery.removeEventListener === 'function') {
      mobileQuery.removeEventListener('change', handleMediaChange)
    } else {
      mobileQuery.removeListener(handleMediaChange)
    }
  }
})
</script>

<template>
  <div
    ref="layoutRef"
    class="xwya-layout ceres-gradient-bg"
    :class="{ 'is-assistant-collapsed': assistantCollapsed, 'is-dragging': isDragging }"
    :style="layoutStyle"
  >
    <main class="xwya-layout-main">
      <slot name="default" :changeNote="changeNote" :assistantCollapsed="assistantCollapsed" />
    </main>

    <div
      v-if="dividerVisible"
      class="xwya-layout-divider"
      role="separator"
      aria-orientation="vertical"
      tabindex="0"
      :title="i18n.global.t('system.dragToResize')"
      @pointerdown="onDividerPointerDown"
      @pointermove="onDividerPointerMove"
      @pointerup="stopDrag"
      @pointercancel="stopDrag"
      @keydown="onDividerKeyDown"
      @dblclick="resetRatio"
    >
      <span class="divider-handle" aria-hidden="true"></span>
    </div>

    <aside class="xwya-assistant ceres-card ceres-glass" :aria-expanded="!assistantCollapsed">
      <header class="xwya-assistant-header">
        <button class="assistant-identity" type="button" :title="assistantTitle" @click="assistantCollapsed && toggleAssistant()">
          <span class="assistant-avatar iconify" :class="assistantIcon" aria-hidden="true"></span>
          <span class="assistant-copy">
            <strong>{{ assistantTitle }}</strong>
            <small v-if="!props.hideAssistantDescription">{{ assistantDescription }}</small>
          </span>
        </button>

        <div class="assistant-actions">
          <button
            v-if="!flag && !assistantCollapsed"
            class="ceres-icon-button assistant-action"
            type="button"
            :title="i18n.global.t('system.clearChat')"
            :aria-label="i18n.global.t('system.clearChat')"
            @click="clearMessages"
          >
            <span class="iconify solar--trash-bin-trash-bold" aria-hidden="true"></span>
          </button>
          <button
            v-if="!assistantCollapsed"
            class="ceres-icon-button assistant-action"
            type="button"
            :title="flag ? chatAssistantTitle : i18n.global.t('pastlist.notes')"
            :aria-label="flag ? chatAssistantTitle : i18n.global.t('pastlist.notes')"
            @click="changeNote"
          >
            <span class="iconify" :class="flag ? 'solar--chat-round-line-bold' : 'solar--notes-bold'" aria-hidden="true"></span>
          </button>
          <button
            class="ceres-icon-button assistant-action"
            type="button"
            :title="assistantCollapsed ? i18n.global.t('chat.expandAssistant') : i18n.global.t('chat.collapseAssistant')"
            :aria-label="assistantCollapsed ? i18n.global.t('chat.expandAssistant') : i18n.global.t('chat.collapseAssistant')"
            @click="toggleAssistant"
          >
            <span class="iconify" :class="assistantCollapsed ? 'solar--sidebar-code-bold' : 'solar--sidebar-minimalistic-bold'" aria-hidden="true"></span>
          </button>
        </div>
      </header>

      <div v-show="!assistantCollapsed" class="xwya-assistant-body">
        <XwyaEditor v-show="flag" v-bind="$attrs" />
        <XwyaChart
          v-if="props.disable"
          v-show="!flag"
          :msgList="msgList"
          :onEnter="onMessage"
          :onClear="clearMessages"
          :modelType="modelType"
          :hideHeader="props.hideChatHeader"
          :assistantRoleLabel="props.chatAssistantRoleLabel"
        />
        <XwyaChart
          v-else
          v-show="!flag"
          :msgList="msgList"
          :onEnter="onEnter"
          :onClear="clearMessages"
          :modelType="modelType"
          :hideHeader="props.hideChatHeader"
          :assistantRoleLabel="props.chatAssistantRoleLabel"
        />
      </div>
    </aside>
  </div>
</template>

<style lang="scss" scoped>
.xwya-layout {
  --assistant-width: clamp(360px, 28vw, 420px);
  --divider-size: 16px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) var(--assistant-width);
  gap: 16px;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  padding: clamp(12px, 1.6vw, 18px);
  color: var(--ceres-fg);
  transition: grid-template-columns 240ms ease;
}

.xwya-layout.is-dragging {
  transition: none;
  user-select: none;
  cursor: col-resize;
}

.xwya-layout.is-dragging .xwya-layout-main,
.xwya-layout.is-dragging .xwya-assistant {
  pointer-events: none;
}

.xwya-layout-divider {
  position: relative;
  z-index: 10;
  display: flex;
  width: var(--divider-size);
  height: 100%;
  min-height: 0;
  align-items: center;
  justify-content: center;
  cursor: col-resize;
  user-select: none;
  touch-action: none;
  flex-shrink: 0;
}

.xwya-layout-divider::before {
  content: '';
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 50%;
  width: 2px;
  transform: translateX(-50%);
  border-radius: 2px;
  background: rgba(var(--ceres-primary-rgb), 0.12);
  transition: background 180ms ease;
}

.xwya-layout-divider .divider-handle {
  position: relative;
  width: 4px;
  height: 48px;
  border-radius: 4px;
  background: rgba(var(--ceres-primary-rgb), 0.3);
  box-shadow: 0 2px 8px rgba(var(--ceres-primary-rgb), 0.12);
  transition: background 180ms ease, height 180ms ease, width 180ms ease;
}

.xwya-layout-divider:hover::before,
.xwya-layout-divider:focus-visible::before,
.xwya-layout.is-dragging .xwya-layout-divider::before {
  background: rgba(var(--ceres-primary-rgb), 0.38);
}

.xwya-layout-divider:hover .divider-handle,
.xwya-layout-divider:focus-visible .divider-handle,
.xwya-layout.is-dragging .divider-handle {
  width: 6px;
  height: 72px;
  background: rgba(var(--ceres-primary-rgb), 0.7);
}

.xwya-layout-divider:focus-visible {
  outline: 3px solid rgba(var(--ceres-primary-rgb), 0.22);
  outline-offset: -3px;
  border-radius: 8px;
}

.xwya-layout.is-assistant-collapsed {
  grid-template-columns: minmax(0, 1fr) 72px;
}

.xwya-layout-main {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  border-radius: calc(var(--ceres-radius) + 2px);
}

.xwya-assistant {
  display: flex;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  flex-direction: column;
  border-color: rgba(var(--ceres-primary-rgb), 0.16);
  background:
    linear-gradient(180deg, rgba(var(--ceres-primary-rgb), 0.08), rgba(8, 145, 178, 0.05)),
    rgba(var(--ceres-card-rgb), 0.9);
  transition: width 240ms ease, max-height 240ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.xwya-assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border-bottom: 1px solid rgba(var(--ceres-primary-rgb), 0.12);
}

.assistant-identity {
  display: inline-flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 10px;
  border: 0;
  background: transparent;
  color: inherit;
  padding: 0;
  text-align: left;
}

.assistant-avatar {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #722ed1, #8b47ea 58%, #0891b2);
  color: #fff;
  font-size: 22px;
  box-shadow: 0 12px 30px rgba(var(--ceres-primary-rgb), 0.24);
}

.assistant-copy {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.assistant-copy strong {
  overflow: hidden;
  color: var(--ceres-fg);
  font-size: 15px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assistant-copy small {
  overflow: hidden;
  color: var(--ceres-muted);
  font-size: 12px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assistant-actions {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}

.assistant-action {
  width: 36px;
  height: 36px;
  cursor: pointer;
}

.assistant-action:focus-visible,
.assistant-identity:focus-visible {
  outline: 3px solid rgba(var(--ceres-primary-rgb), 0.22);
  outline-offset: 3px;
}

.xwya-assistant-body {
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

.is-assistant-collapsed .xwya-assistant-header {
  flex-direction: column;
  padding: 10px;
}

.is-assistant-collapsed .assistant-identity {
  justify-content: center;
  cursor: pointer;
}

.is-assistant-collapsed .assistant-copy {
  display: none;
}

.is-assistant-collapsed .assistant-actions {
  flex-direction: column;
}

@media (max-width: 900px) {
  .xwya-layout,
  .xwya-layout.is-assistant-collapsed {
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: minmax(0, 1fr) auto;
    gap: 12px;
    padding: 10px;
  }

  .xwya-layout-divider {
    display: none;
  }

  .xwya-assistant {
    width: 100%;
    max-height: min(46vh, 420px);
  }

  .xwya-layout.is-assistant-collapsed .xwya-assistant {
    max-height: 68px;
  }

  .is-assistant-collapsed .xwya-assistant-header {
    flex-direction: row;
  }

  .is-assistant-collapsed .assistant-actions {
    flex-direction: row;
  }
}

@media (max-width: 560px) {
  .xwya-layout {
    padding: 8px;
  }

  .xwya-assistant-header {
    padding: 10px;
  }

  .assistant-avatar {
    width: 38px;
    height: 38px;
    flex-basis: 38px;
    border-radius: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .xwya-layout,
  .xwya-assistant,
  .ceres-icon-button {
    transition: none;
  }
}
</style>

<style lang="scss">
body.xwya-resizing {
  user-select: none;
  -webkit-user-select: none;
  cursor: col-resize;
}
</style>
