<script lang="ts" setup>
import { clearChat, textToAudio } from '@/api/chat'
import Media from '@/utils/media'
import useSystemStore from '@/store/useSystemStore'
import { marked } from 'marked'
import '@/utils/mermaid'
import { renderMermaidIn } from '@/utils/mermaid'
import i18n from '@/lang'
import img2 from '@/assets/image/2.svg'
import img3 from '@/assets/image/3.svg'
import img4 from '@/assets/image/4.svg'
import img5 from '@/assets/image/5.svg'
import img6 from '@/assets/image/6.svg'
import img7 from '@/assets/image/7.svg'
import img8 from '@/assets/image/8.svg'
import img9 from '@/assets/image/9.svg'

const imginfo = {
  2: img2,
  3: img3,
  4: img4,
  5: img5,
  6: img6,
  7: img7,
  8: img8,
  9: img9
}

const props = defineProps({
  msgList: {
    type: [Array, Object],
    default: () => []
  },
  isBnt: {
    type: Boolean,
    default: false
  },
  size: {
    type: Number,
    default: 14
  },
  next: {
    type: Function,
    default: () => {}
  },
  show: {
    type: Function,
    default: () => {}
  },
  onClear: {
    type: Function,
    default: () => {}
  },
  modelType: {
    type: Number,
    default: 0
  },
  hideHeader: {
    type: Boolean,
    default: false
  },
  assistantRoleLabel: {
    type: String,
    default: ''
  }
})
const systemStore = useSystemStore()
const isChangeImg = ref(false)
const scrollRef = ref<any>(null)
const contentRef = ref<any>(null)
const imageRef = ref<any>(null)
const observer = shallowRef<IntersectionObserver | null>(null)
const showImage = ref('')
let mediaRecorder = null
const isRecognizing = ref(false)
const userInfo = computed<any>(() => systemStore.userInfo || {})
const hasPendingImage = computed(() => Boolean(contentRef.value?.pendingImageMinioUrl))

const openImagePreview = (src: string) => {
  showImage.value = src
  nextTick(() => {
    const imageNode = imageRef.value?.$el?.querySelector?.('img') || imageRef.value?.$el || imageRef.value
    imageNode?.click?.()
  })
}

const getScrollContainer = (): HTMLElement | null => {
  const scrollContent = document.querySelector('.xwya-scroll')
  return (scrollContent?.parentNode as HTMLElement) || null
}

const getScrollHeight = (): number => {
  const scroll = getScrollContainer()
  if (!scroll) return 0
  if (!isChangeImg.value) {
    startObserver(scroll)
  }
  changeImg(scroll)
  return scroll.scrollHeight
}
const scrollBottom = () => {
  if (scrollRef.value) {
    scrollRef.value.scrollTo({ top: getScrollHeight(), behavior: 'smooth' })
  }
}
const startObserver = (ElDom: HTMLElement) => {
  isChangeImg.value = true
  observer.value = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return
      const element = entry.target as HTMLElement
      const img = element instanceof HTMLImageElement ? element : element.querySelector('img')
      if (!img) {
        observer.value?.unobserve(element)
        return
      }
      img.style.width = '100%'
      img.classList.add('message-preview-image')
      img.addEventListener('click', () => openImagePreview(img.src))
      observer.value?.unobserve(element)
    })
  }, { root: ElDom })
}
const changeImg = (ElDom: HTMLElement) => {
  if (!observer.value) return
  ElDom.querySelectorAll('.n-image, .xwya-message-image, .text-content img').forEach((element) => {
    const target = element as HTMLElement
    if (target.dataset.observed) return
    target.dataset.observed = 'true'
    observer.value?.observe(target)
  })
}

const thumbUp = (id: number, up: number) => {
  props.msgList.find(item => item.historyId === id).up = up
  fetch(import.meta.env.VITE_APP_URL + '/chat/up?id=' + id + '&up=' + up, {
    method: 'GET',
    headers: {
      'Authorization': localStorage.getItem('token'),
    }
  })
}
const initMedia = () => {
  mediaRecorder = new Media()
  mediaRecorder.onSuccess(async (b: Blob) => {
    const file = new File([b], 'audio.mp3', { type: b.type })
    const d = new FormData()
    d.append('audio', file)

    const res = await fetch(import.meta.env.VITE_APP_URL + '/chat/audioTrans2Text', {
      method: 'POST',
      headers: {
        'Authorization': localStorage.getItem('token'),
      },
      body: d,
    })
    const json = await res.json()
    isRecognizing.value = false
    if (json.code === 200) {
      contentRef.value.addContent(json.data)
    }
  })
  mediaRecorder.onError(() => {
    isRecognizing.value = false
  })
}

const text2sound = async text => {
  const data = await textToAudio({ text, langue: 'en' })
  const audio = new Audio()
  audio.src = 'data:audio/wav;base64,' + data.data
  audio.play()
}

const startAudio = async () => {
  if (isRecognizing.value) {
    window.$msg.info(i18n.global.t('chat.endrecord'))
    return
  }
  isRecognizing.value = mediaRecorder ? await mediaRecorder.start() : false
}

const handleUpload = () => {
  contentRef.value?.triggerImageUpload?.()
}

const submitMessage = () => {
  contentRef.value?.submitContent?.()
}

const handleClearChat = async () => {
  const res = await clearChat({ modelType: props.modelType })
  if (res.code === 200) {
    props.onClear()
    window.$msg?.success?.(i18n.global.t('system.cleared'))
  }
}

const renderContent = (content = '') => marked(content || '') as string

const getRoleLabel = (role: string) => {
  if (role === 'user') return i18n.global.t('chat.you')
  if (role === 'patient') return i18n.global.t('chat.stdpatient')
  if (props.assistantRoleLabel) return props.assistantRoleLabel
  return i18n.global.t('chat.assistant')
}

const getRoleIcon = (role: string) => {
  if (role === 'user') return 'solar--user-rounded-bold'
  if (role === 'patient') return 'solar--clipboard-heart-bold'
  return 'solar--chat-round-line-bold'
}

const getAvatar = (message) => {
  if (message.role === 'user') return userInfo.value.avatar
  if (message.role === 'patient') return imginfo[message.picture]
  if (message.picture === 1) return userInfo.value.ps?.[userInfo.value.teacherPicture]
  return message.avatar || ''
}

const getStatusLabel = (message) => {
  if (message.loading) return i18n.global.t('chat.generate')
  if (message.temp) return i18n.global.t('chat.realtime')
  if (message.role === 'user') return i18n.global.t('chat.sent')
  if (message.role === 'patient') return i18n.global.t('chat.case')
  return i18n.global.t('chat.response')
}

const getMessageTime = (message) => message.createTime || message.time || message.updateTime || ''

defineExpose({
  handleClearChat
})

const messageListRef = ref<HTMLElement | null>(null)

watch(() => props.msgList, () => {
  nextTick(() => {
    scrollBottom()
    renderMermaidIn(messageListRef.value)
  })
}, { deep: true })
onMounted(() => {
  initMedia()
  scrollBottom()
  nextTick(() => renderMermaidIn(messageListRef.value))
})
onBeforeUnmount(() => {
  observer.value && observer.value?.disconnect()
})
</script>

<template>
  <section class="ai-chat-shell ceres-card" :class="{ 'is-case-mode': isBnt }">
    <header v-if="!hideHeader" class="chat-panel-header">
      <div class="assistant-title-block">
        <span class="assistant-icon iconify solar--chat-round-line-bold"></span>
        <div>
          <p class="assistant-kicker">{{ i18n.global.t('chat.clinicalAgent') }}</p>
          <h2>{{ i18n.global.t('chat.medicaldialogue') }}</h2>
        </div>
      </div>
      <div class="assistant-status ceres-pill" :class="{ 'is-recording': isRecognizing }">
        <span class="status-dot"></span>
        {{ isRecognizing ? i18n.global.t('chat.recognizingspeech') : i18n.global.t('chat.ready') }}
      </div>
    </header>

    <n-scrollbar ref="scrollRef" content-class="xwya-scroll" class="message-scroll">
      <div ref="messageListRef" class="message-list">
        <div v-if="msgList.length === 0" class="empty-chat-state">
          <span class="empty-icon iconify solar--medical-kit-bold"></span>
          <p>{{ i18n.global.t('chat.startconversation') }}</p>
        </div>

        <article v-for="(p, i) in msgList" :key="p.id || p.historyId || i" class="message-row" :class="[`is-${p.role}`, { 'is-loading': p.loading }]">
          <img v-if="getAvatar(p)" :src="getAvatar(p)" class="message-avatar" alt="" />
          <div v-else class="message-avatar avatar-fallback">
            <span class="iconify" :class="getRoleIcon(p.role)"></span>
          </div>

          <div class="message-body">
            <div class="message-meta">
              <span class="message-name">{{ getRoleLabel(p.role) }}</span>
              <span v-if="getMessageTime(p)" class="message-time">{{ getMessageTime(p) }}</span>
              <span class="message-badge">{{ getStatusLabel(p) }}</span>
            </div>

            <div v-if="!p.loading" class="message-content-card text-content" :style="{ fontSize: size + 'px' }" v-html="renderContent(p.content)"></div>
            <div v-else class="message-content-card loading-card">
              <n-spin size="small" />
              <span>{{ i18n.global.t('chat.analyzecase') }}</span>
            </div>

            <div v-if="p.role === 'system' && p.up >= 0 && !p.loading" class="message-actions">
              <button type="button" class="message-action-button" :title="i18n.global.t('chat.speak')" aria-label="i18n.global.t('chat.speak')" @click="text2sound(p.content)">
                <span class="iconify ic--outline-spatial-audio-off"></span>
              </button>
              <button
                type="button"
                class="message-action-button"
                :class="{ active: p.up == 1 }"
                :title="i18n.global.t('chat.helpful')"
                :aria-label="i18n.global.t('chat.helpful')"
                @click="p.up == 1 ? null : thumbUp(p.historyId, 1)"
              >
                <span class="iconify" :class="p.up == 1 ? 'ic--baseline-thumb-up-alt' : 'ic--baseline-thumb-up-off-alt'"></span>
              </button>
              <button
                type="button"
                class="message-action-button"
                :class="{ active: p.up == 2 }"
                :title="i18n.global.t('chat.unhelpful')"
                :aria-label="i18n.global.t('chat.unhelpful')"
                @click="p.up == 2 ? null : thumbUp(p.historyId, 2)"
              >
                <span class="iconify" :class="p.up == 2 ? 'ic--baseline-thumb-down-alt' : 'ic--baseline-thumb-down-off-alt'"></span>
              </button>
            </div>
          </div>
        </article>
      </div>
    </n-scrollbar>

    <footer class="prompt-footer">
      <div v-if="isBnt" class="case-action-strip">
        <div class="case-action-copy">
          <span class="iconify solar--routing-3-bold"></span>
          <span>{{ i18n.global.t('chat.casereasoning') }}</span>
        </div>
        <div class="case-action-buttons">
          <button type="button" class="case-step-button secondary" @click="show">
            <span class="iconify solar--eye-bold"></span>
            {{ $t('case.bnt1') }}
          </button>
          <button type="button" class="case-step-button primary" @click="next">
            <span class="iconify solar--arrow-right-up-bold"></span>
            {{ $t('case.bnt2') }}
          </button>
        </div>
      </div>

      <div class="prompt-card ceres-glass" :class="{ 'is-recording': isRecognizing, 'has-attachment': hasPendingImage }">
        <XwyaEditorContent ref="contentRef" class="prompt-editor" v-bind="$attrs" :isEnter="isRecognizing"></XwyaEditorContent>

        <div class="prompt-button-row">
          <div class="prompt-tools">
            <button
              type="button"
              class="prompt-icon-button ceres-icon-button"
              :class="{ active: isRecognizing }"
              :aria-pressed="isRecognizing"
              :title="i18n.global.t('chat.voiceinput')"
              :aria-label="i18n.global.t('chat.voiceinput')"
              @click="startAudio"
            >
              <span class="iconify ic--baseline-mic"></span>
            </button>
            <button
              type="button"
              class="prompt-icon-button ceres-icon-button"
              :class="{ active: hasPendingImage }"
              :aria-pressed="hasPendingImage"
              :title="$t('system.uploadImage')"
              :aria-label="$t('system.uploadImage')"
              @click="handleUpload"
            >
              <span class="iconify ic--baseline-image"></span>
            </button>
          </div>

          <div class="prompt-actions">
            <n-popconfirm
              :positive-text="$t('system.clearConfirm')"
              :negative-text="$t('system.clearCancel')"
              @positive-click="handleClearChat"
            >
              {{ $t('system.confirmClearChat') }}
              <template #trigger>
                <button type="button" class="prompt-icon-button clear-button ceres-icon-button" :title="$t('system.clearChat')" :aria-label="$t('system.clearChat')">
                  <span class="iconify ic--baseline-delete-outline"></span>
                </button>
              </template>
            </n-popconfirm>
            <button type="button" class="send-button ceres-icon-button" :title="$t('chat.send')" :aria-label="$t('chat.send')" @click="submitMessage">
              <span class="iconify solar--arrow-up-linear"></span>
            </button>
          </div>
        </div>
      </div>
    </footer>

    <n-image ref="imageRef" v-show="false" width="100" :src="showImage" :previewed-img-props="{ style: { border: '8px solid white' } }" />
  </section>
</template>

<style lang="css" scoped>
.ai-chat-shell {
  display: flex;
  min-height: 0;
  height: 100%;
  flex-direction: column;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(var(--ceres-card-rgb), 0.96), rgba(var(--ceres-card-rgb), 0.9)),
    var(--ceres-card);
}

.chat-panel-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid var(--ceres-border);
  padding: 14px 16px;
}

.assistant-title-block {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.assistant-icon {
  display: inline-flex;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-cyan));
  color: #fff;
  font-size: 22px;
  box-shadow: 0 14px 30px rgba(var(--ceres-primary-rgb), 0.22);
}

.assistant-kicker {
  color: var(--ceres-muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.assistant-title-block h2 {
  margin: 1px 0 0;
  color: var(--ceres-fg);
  font-size: 17px;
  line-height: 1.2;
}

.assistant-status {
  min-height: 32px;
  flex-shrink: 0;
  padding: 4px 11px;
  color: var(--ceres-muted);
  font-size: 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--ceres-emerald);
  box-shadow: 0 0 0 4px rgba(5, 150, 105, 0.12);
}

.assistant-status.is-recording {
  border-color: rgba(var(--ceres-primary-rgb), 0.36);
  color: var(--ceres-primary);
}

.message-scroll {
  min-height: 0;
  flex: 1;
}

.message-list {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
}

.empty-chat-state {
  display: grid;
  min-height: 220px;
  flex: 1;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: var(--ceres-muted);
  text-align: center;
}

.empty-icon {
  display: inline-flex;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.18);
  border-radius: 18px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  font-size: 28px;
}

.message-row {
  display: flex;
  width: 100%;
  align-items: flex-start;
  gap: 10px;
  animation: message-enter 220ms ease-out both;
}

.message-row.is-user {
  justify-content: flex-end;
}

.message-row.is-user .message-avatar {
  order: 2;
}

.message-row.is-user .message-body {
  align-items: flex-end;
  order: 1;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  overflow: hidden;
  border: 1px solid var(--ceres-border);
  border-radius: 999px;
  background: rgba(var(--ceres-card-rgb), 0.9);
  object-fit: cover;
}

.avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--ceres-primary);
  font-size: 20px;
}

.message-body {
  display: flex;
  min-width: 0;
  max-width: min(760px, calc(100% - 46px));
  flex-direction: column;
  align-items: flex-start;
  gap: 7px;
}

.message-meta {
  display: flex;
  max-width: 100%;
  flex-wrap: wrap;
  align-items: center;
  gap: 7px;
  color: var(--ceres-muted);
  font-size: 12px;
}

.message-name {
  color: var(--ceres-fg);
  font-weight: 800;
}

.message-time {
  color: var(--ceres-muted);
}

.message-badge {
  display: inline-flex;
  min-height: 22px;
  align-items: center;
  border-radius: 999px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  padding: 2px 8px;
  font-weight: 800;
}

.message-content-card {
  width: fit-content;
  max-width: 100%;
  overflow: hidden;
  border: 1px solid var(--ceres-border);
  border-radius: 18px;
  background: rgba(var(--ceres-card-rgb), 0.96);
  color: var(--ceres-fg);
  padding: 13px 15px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.06);
}

.message-row.is-system .message-content-card {
  border-color: rgba(var(--ceres-primary-rgb), 0.18);
}

.message-row.is-patient .message-content-card {
  border-color: rgba(8, 145, 178, 0.28);
  background: linear-gradient(135deg, rgba(8, 145, 178, 0.08), rgba(var(--ceres-card-rgb), 0.96));
}

.message-row.is-user .message-content-card {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-primary-hover));
  color: #fff;
  box-shadow: 0 16px 32px rgba(var(--ceres-primary-rgb), 0.24);
}

.loading-card {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--ceres-muted);
}

.text-content {
  position: relative;
  word-break: break-word;
  overflow-wrap: anywhere;
  line-height: 1.72;
}

.text-content :deep(p) {
  margin: 0 0 10px;
}

.text-content :deep(p:last-child) {
  margin-bottom: 0;
}

.text-content :deep(ul),
.text-content :deep(ol) {
  margin: 10px 0 10px 22px;
  padding-left: 14px;
}

.text-content :deep(li + li) {
  margin-top: 5px;
}

.text-content :deep(blockquote) {
  margin: 12px 0;
  border-left: 3px solid var(--ceres-primary);
  border-radius: 8px;
  background: var(--ceres-primary-soft);
  padding: 10px 12px;
  color: var(--ceres-muted);
}

.text-content :deep(a) {
  color: var(--ceres-cyan);
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.text-content :deep(code) {
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.14);
  border-radius: 6px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  padding: 1px 6px;
  font-size: 0.92em;
}

.text-content :deep(pre) {
  max-width: 100%;
  margin: 12px 0;
  overflow-x: auto;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 12px;
  background: #0f172a;
  color: #f8fafc;
  padding: 13px 14px;
  line-height: 1.6;
}

.text-content :deep(pre code) {
  border: 0;
  background: transparent;
  color: inherit;
  padding: 0;
}

.text-content :deep(table) {
  width: 100%;
  margin: 12px 0;
  border-collapse: collapse;
  overflow: hidden;
  border-radius: 10px;
  font-size: 0.94em;
}

.text-content :deep(th),
.text-content :deep(td) {
  border: 1px solid var(--ceres-border);
  padding: 8px 10px;
  text-align: left;
}

.text-content :deep(th) {
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
}

.text-content :deep(img),
.text-content :deep(.message-preview-image) {
  display: block;
  max-width: min(520px, 100%);
  height: auto;
  margin-top: 10px;
  border: 1px solid var(--ceres-border);
  border-radius: 14px;
  cursor: pointer;
  object-fit: contain;
}

.message-row.is-user .text-content :deep(code) {
  border-color: rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.message-row.is-user .text-content :deep(a) {
  color: #dff7ff;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--ceres-border);
  border-radius: 999px;
  background: rgba(var(--ceres-card-rgb), 0.86);
  padding: 4px;
}

.message-action-button {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--ceres-muted);
  cursor: pointer;
  font-size: 18px;
  transition: background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.message-action-button:hover,
.message-action-button:focus-visible,
.message-action-button.active {
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  outline: none;
}

.prompt-footer {
  flex-shrink: 0;
  border-top: 1px solid var(--ceres-border);
  background: linear-gradient(180deg, rgba(var(--ceres-card-rgb), 0.72), rgba(var(--ceres-card-rgb), 0.96));
  padding: 12px;
}

.case-action-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.18);
  border-radius: 14px;
  background: var(--ceres-primary-soft);
  padding: 9px 10px;
}

.case-action-copy {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  color: var(--ceres-primary);
  font-size: 13px;
  font-weight: 800;
}

.case-action-copy .iconify {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.case-action-buttons {
  display: inline-flex;
  flex-shrink: 0;
  gap: 8px;
}

.case-step-button {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 800;
  transition: border-color 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.case-step-button.secondary {
  border-color: rgba(var(--ceres-primary-rgb), 0.22);
  background: rgba(var(--ceres-card-rgb), 0.82);
  color: var(--ceres-primary);
}

.case-step-button.primary {
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-primary-hover));
  color: #fff;
  box-shadow: 0 10px 22px rgba(var(--ceres-primary-rgb), 0.22);
}

.case-step-button:hover,
.case-step-button:focus-visible {
  border-color: rgba(var(--ceres-primary-rgb), 0.42);
  box-shadow: 0 10px 24px rgba(var(--ceres-primary-rgb), 0.16);
  outline: none;
}

.prompt-card {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
  border-color: rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 20px;
  padding: 12px;
  transition: border-color 200ms ease, box-shadow 200ms ease, background-color 200ms ease;
}

.prompt-card.is-recording {
  border-color: rgba(var(--ceres-primary-rgb), 0.52);
  box-shadow: 0 0 0 4px rgba(var(--ceres-primary-rgb), 0.12), var(--ceres-shadow-sm);
  animation: recording-ring 1800ms ease-in-out infinite;
}

.prompt-card.has-attachment {
  border-color: rgba(8, 145, 178, 0.34);
}

.prompt-tools,
.prompt-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}

.prompt-button-row {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.prompt-editor {
  min-width: 0;
  flex: 1;
  width: 100%;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 16px;
  background: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82), 0 8px 22px rgba(15, 23, 42, 0.05);
  color: #111827;
  --prompt-editor-placeholder: #6b7280;
  padding: 12px;
}

.prompt-icon-button {
  width: 38px;
  height: 38px;
  color: var(--ceres-muted);
  font-size: 20px;
}

.prompt-icon-button.active {
  border-color: rgba(var(--ceres-primary-rgb), 0.46);
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
}

.clear-button:hover,
.clear-button:focus-visible {
  border-color: rgba(220, 38, 38, 0.35);
  background: rgba(220, 38, 38, 0.09);
  color: var(--ceres-danger);
}

.send-button {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-primary-hover));
  color: #fff;
  box-shadow: 0 12px 26px rgba(var(--ceres-primary-rgb), 0.24);
}

.send-button:hover,
.send-button:focus-visible {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ceres-primary-hover), var(--ceres-primary));
  color: #fff;
}

@keyframes message-enter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes recording-ring {
  0%, 100% {
    box-shadow: 0 0 0 4px rgba(var(--ceres-primary-rgb), 0.1), var(--ceres-shadow-sm);
  }
  50% {
    box-shadow: 0 0 0 7px rgba(var(--ceres-primary-rgb), 0.18), var(--ceres-shadow);
  }
}

@media (max-width: 768px) {
  .chat-panel-header,
  .case-action-strip,
  .prompt-card {
    align-items: stretch;
  }

  .chat-panel-header {
    flex-direction: column;
  }

  .assistant-status {
    width: fit-content;
  }

  .message-list {
    padding: 14px;
  }

  .message-body {
    max-width: calc(100% - 42px);
  }

  .case-action-strip,
  .prompt-card {
    flex-direction: column;
  }

  .prompt-button-row {
    align-items: stretch;
    flex-direction: column;
  }

  .case-action-buttons,
  .prompt-tools,
  .prompt-actions {
    width: 100%;
  }

  .case-step-button,
  .prompt-icon-button,
  .send-button {
    flex: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .message-row,
  .prompt-card.is-recording {
    animation: none;
  }

  .message-action-button,
  .case-step-button,
  .prompt-card {
    transition: none;
  }
}
</style>
