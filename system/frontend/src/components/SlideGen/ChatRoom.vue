<template>
  <section class="slidegen-workspace">
    <div v-if="!isConnected" class="connection-panel ceres-page-shell">
      <div class="connection-card ceres-card">
        <div class="connection-copy">
          <p class="eyebrow">{{ $t('slideGen.workspace') }}</p>
          <h1>{{ $t('slideGen.title') }}</h1>
          <p>{{ $t('slideGen.intro') }}</p>
        </div>

        <div class="connection-details">
          <div class="user-preview">
            <img v-if="userAvatarPath" :src="userAvatarPath" alt="User Avatar" class="preview-avatar" />
            <div v-else class="preview-avatar avatar-fallback">{{ userInitial }}</div>
            <div>
              <span class="preview-label">{{ $t('slideGen.currentUser') }}</span>
              <strong>{{ displayName }}</strong>
            </div>
          </div>

          <div class="connection-status" :class="{ error: connectionError, connecting: isConnecting }">
            <span class="status-dot"></span>
            <span>{{ connectionError || (isConnecting ? $t('slideGen.connectingService') : $t('slideGen.readyService')) }}</span>
          </div>
        </div>

        <button type="button" class="connect-button" :disabled="!userId || isConnecting" @click="connect">
          <span v-if="isConnecting" class="loading-spinner"></span>
          <span v-else class="iconify solar--play-circle-bold"></span>
          {{ isConnecting ? $t('slideGen.connecting') : connectionError ? $t('slideGen.reconnect') : $t('slideGen.startGenerate') }}
        </button>
      </div>
    </div>

    <div v-else class="workspace-grid ceres-page-shell">
      <header class="task-status-bar ceres-glass">
        <div class="task-status-main">
          <span class="live-indicator" :class="stateTone"></span>
          <div>
            <p class="eyebrow">{{ $t('slideGen.generation') }}</p>
            <h2>{{ stateLabel }}</h2>
            <span>{{ statusDescription }}</span>
          </div>
        </div>

        <div class="task-actions">
          <span v-if="currentSlideId" class="slide-id ceres-pill">Slide ID: {{ currentSlideId }}</span>
          <button type="button" class="task-button secondary" @click="disconnect">
            <span class="iconify solar--restart-bold"></span>
            {{ $t('slideGen.reconnect') }}
          </button>
        </div>
      </header>

      <main class="conversation-panel ceres-card">
        <div class="conversation-header">
          <div class="conversation-title">
            <span class="panel-icon iconify solar--chat-round-line-bold"></span>
            <div>
              <h3>{{ $t('slideGen.messageFlow') }}</h3>
              <p>{{ activeStageLabel }}</p>
            </div>
          </div>
          <span class="message-count">{{ $t('slideGen.messageCount', { count: filteredMessages.length }) }}</span>
        </div>

        <div class="messages-area" ref="messagesContainer">
          <div v-if="filteredMessages.length === 0" class="empty-chat">
            <span class="iconify solar--document-add-bold"></span>
            <h3>{{ $t('slideGen.emptyTitle') }}</h3>
            <p>{{ $t('slideGen.emptyDescription') }}</p>
          </div>

          <template v-for="(msg, index) in filteredMessages" :key="`${msg.type}-${msg.timestamp || index}`">
            <article v-if="msg.type === 'slide_complete_action'" class="status-card complete">
              <div class="status-card-content">
                <span class="status-card-icon iconify solar--check-circle-bold"></span>
                <div>
                  <div class="status-card-header">
                    <strong>{{ $t('slideGen.completeTitle') }}</strong>
                    <time>{{ formatTime(msg.timestamp) }}</time>
                  </div>
                  <p>{{ $t('slideGen.completeDescription') }}</p>
                </div>
              </div>
              <button type="button" class="status-action success" @click="previewSlide">
                <span class="iconify solar--eye-bold"></span>
                {{ $t('slideGen.previewSlide') }}
              </button>
            </article>

            <article v-else-if="msg.type === 'outline_action'" class="status-card outline">
              <div class="status-card-content">
                <img :src="slideGenAgentAvatar('DesignerAgent')" alt="DesignerAgent" class="status-avatar" />
                <div>
                  <div class="status-card-header">
                    <strong>{{ $t('slideGen.outlineComplete') }}</strong>
                    <time>{{ formatTime(msg.timestamp) }}</time>
                  </div>
                  <p>{{ $t('slideGen.outlineDescription') }}</p>
                </div>
              </div>
              <button type="button" class="status-action" @click="previewOutline(msg.content)">
                <span class="iconify solar--eye-bold"></span>
                {{ $t('slideGen.previewOutline') }}
              </button>
            </article>

            <article v-else-if="msg.type === 'need_input'" class="status-card waiting">
              <div class="status-card-content">
                <span class="status-card-icon iconify solar--question-circle-bold"></span>
                <div>
                  <div class="status-card-header">
                    <strong>{{ $t('slideGen.waitingInput') }}</strong>
                    <time>{{ formatTime(msg.timestamp) }}</time>
                  </div>
                  <p>{{ msg.content }}</p>
                </div>
              </div>
            </article>

            <SlideGenAgentMessage
              v-else
              :agent-name="msg.agent_name"
              :agent-type="msg.agent_type"
              :content="msg.content"
              :timestamp="msg.timestamp"
              :message-type="msg.type"
              :user-avatar="userAvatarPath"
            />
          </template>
        </div>

        <SlideGenUserInput :disabled="isProcessing" :placeholder="inputPlaceholder" @send="sendMessage" />
      </main>

      <aside class="resource-panel ceres-card">
        <section class="panel-section">
          <div class="panel-section-header">
            <span class="panel-icon iconify solar--routing-3-bold"></span>
            <div>
              <h3>{{ $t('slideGen.progress') }}</h3>
              <p>{{ stateLabel }}</p>
            </div>
          </div>

          <ol class="timeline">
            <li v-for="step in workflowSteps" :key="step.key" :class="step.status">
              <span class="timeline-dot">
                <span v-if="step.status === 'done'" class="iconify solar--check-circle-bold"></span>
                <span v-else-if="step.status === 'active'" class="iconify eos-icons--three-dots-loading"></span>
                <span v-else-if="step.status === 'waiting'" class="iconify solar--clock-circle-bold"></span>
              </span>
              <div>
                <strong>{{ step.label }}</strong>
                <p>{{ step.description }}</p>
              </div>
            </li>
          </ol>
        </section>

        <section class="panel-section resources-summary">
          <button type="button" class="panel-primary-button" :disabled="!currentSlideId" @click="openResources">
            <span class="iconify solar--folder-open-bold"></span>
            {{ $t('slideGen.openResources') }}
          </button>
        </section>

        <section class="panel-section state-panel" :class="stateTone">
          <span class="iconify solar--shield-check-bold"></span>
          <div>
            <strong>{{ stateLabel }}</strong>
            <p>{{ statusDescription }}</p>
          </div>
        </section>
      </aside>
    </div>

    <div v-if="showPreviewModal" class="modal-overlay" @click.self="closePreview">
      <section class="preview-modal ceres-card" role="dialog" aria-modal="true" :aria-label="$t('slideGen.previewAria')">
        <header class="modal-header">
          <div>
            <p class="eyebrow">Preview</p>
            <h3>{{ $t('slideGen.previewAria') }}</h3>
          </div>
          <button type="button" class="close-button ceres-icon-button" :aria-label="$t('slideGen.closePreview')" @click="closePreview">
            <span class="iconify solar--close-circle-bold"></span>
          </button>
        </header>
        <div ref="previewBodyRef" class="preview-body" v-html="renderedPreviewContent"></div>
      </section>
    </div>

    <div v-if="showResourcesModal" class="modal-overlay" @click.self="closeResources">
      <section class="resources-modal ceres-card" role="dialog" aria-modal="true" :aria-label="$t('slideGen.resourcesAria')">
        <header class="modal-header resources">
          <div>
            <p class="eyebrow">{{ $t('slideGen.assets') }}</p>
            <h3>{{ $t('slideGen.resourcesAria') }}</h3>
          </div>
          <button type="button" class="close-button ceres-icon-button" :aria-label="$t('slideGen.closeResources')" @click="closeResources">
            <span class="iconify solar--close-circle-bold"></span>
          </button>
        </header>

        <div class="resources-body">
          <section class="resource-section">
            <div class="section-header">
              <div class="section-title">
                <span class="section-icon iconify solar--document-text-bold"></span>
                <div>
                  <h4>{{ $t('slideGen.document') }}</h4>
                  <p class="file-name">slide_draft.md</p>
                </div>
              </div>
              <div class="section-actions">
                <button type="button" class="resource-action" @click="previewMarkdownFromResources">
                  <span class="iconify solar--eye-bold"></span>
                  {{ $t('slideGen.preview') }}
                </button>
                <button type="button" class="resource-action" @click="downloadMarkdown">
                  <span class="iconify solar--download-minimalistic-bold"></span>
                  {{ $t('slideGen.download') }}
                </button>
              </div>
            </div>
          </section>

          <section class="resource-section">
            <div class="section-header">
              <div class="section-title">
                <span class="section-icon indigo iconify solar--presentation-graph-bold"></span>
                <div>
                  <h4>{{ $t('slideGen.pptPresentation') }}</h4>
                  <p class="file-name">slide.pptx</p>
                </div>
              </div>
              <div class="section-actions">
                <button v-if="pptExists" type="button" class="resource-action primary" @click="downloadPPT">
                  <span class="iconify solar--download-minimalistic-bold"></span>
                  {{ $t('slideGen.downloadPpt') }}
                </button>
                <span v-else class="pending-pill">{{ $t('slideGen.notGenerated') }}</span>
              </div>
            </div>
          </section>

          <section class="resource-section image-section">
            <div class="section-header">
              <div class="section-title">
                <span class="section-icon cyan iconify solar--gallery-bold"></span>
                <div>
                  <h4>{{ $t('slideGen.imageResources') }}</h4>
                  <p class="file-name">{{ $t('slideGen.imageCount', { count: resourceImages.length }) }}</p>
                </div>
              </div>
              <button v-if="resourceImages.length > 0" type="button" class="resource-action" @click="downloadAllImages">
                <span class="iconify solar--download-minimalistic-bold"></span>
                {{ $t('slideGen.allDownload') }}
              </button>
            </div>

            <div v-if="isLoadingResources" class="loading-state">
              <span class="loading-spinner dark"></span>
              {{ $t('slideGen.loadingResources') }}
            </div>

            <div v-else-if="resourceImages.length === 0" class="empty-state">
              <span class="iconify solar--gallery-remove-bold"></span>
              {{ $t('slideGen.noImages') }}
            </div>

            <div v-else class="images-grid">
              <article v-for="img in resourceImages" :key="img.image_id" class="image-card">
                <div class="image-wrapper">
                  <img :src="getImageUrl(img.image_id)" :alt="`Image ${img.image_id}`" />
                  <div class="image-overlay">
                    <p><strong>ID</strong>{{ img.image_id }}</p>
                    <p><strong>{{ $t('slideGen.position') }}</strong>{{ img.position }}</p>
                    <p><strong>{{ $t('slideGen.query') }}</strong>{{ img.query }}</p>
                  </div>
                </div>
                <button type="button" class="image-download" :title="$t('slideGen.downloadImage')" :aria-label="$t('slideGen.downloadImage')" @click="downloadImage(img.image_id)">
                  <span class="iconify solar--download-minimalistic-bold"></span>
                </button>
              </article>
            </div>
          </section>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { marked } from 'marked'
import '@/utils/mermaid'
import { renderMermaidIn } from '@/utils/mermaid'
import useSystemStore from '@/store/useSystemStore'
import { getSlideGenWsUrl, slideResourceUrl } from '@/utils/slideGenApi'
import { slideGenAgentAvatar } from '@/utils/slideGenAvatars'
import { isFinalResourceStatus, isResourceReadyStatus, normalizeSlideImages, type SlideResourceImage } from '@/utils/slideGenResources'
import i18n from '@/lang'
import SlideGenAgentMessage from './AgentMessage.vue'
import SlideGenUserInput from './UserInput.vue'

interface SlideGenMessage {
  type: string
  agent_name?: string
  agent_type?: string
  content: string
  timestamp?: string
}

const emit = defineEmits(['connection-changed'])

const systemStore = useSystemStore()
const t = (key: string, params?: Record<string, unknown>) => i18n.global.t(key, params as any)
const timeLocale = computed(() => {
  const lang = String(i18n.global.locale || systemStore.lang || '').toLowerCase()
  if (lang.startsWith('en')) return 'en-US'
  if (lang.includes('malay') || lang.startsWith('ms')) return 'ms-MY'
  if (lang.includes('hindi') || lang.startsWith('hi')) return 'hi-IN'
  return 'zh-CN'
})
const userId = computed(() => String(systemStore.userInfo?.userId ?? ''))
const userName = computed(() => systemStore.userInfo?.userName || systemStore.userInfo?.nickName || t('slideGen.userFallback'))
const displayName = computed(() => userName.value)
const userAvatarPath = computed(() => systemStore.userInfo?.avatar || '')
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase() || 'U')
const activeLocale = computed(() => String(i18n.global.locale || systemStore.lang || 'zhcn'))

const connectionError = ref('')
const isConnected = ref(false)
const isConnecting = ref(false)
const isProcessing = ref(false)
const needInputPrompt = ref('')
const messages = ref<SlideGenMessage[]>([])
const currentSlideId = ref('')
const awaitingFeedback = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const showPreviewModal = ref(false)
const previewContent = ref('')
const showResourcesModal = ref(false)
const resourceImages = ref<ResourceImage[]>([])
const isLoadingResources = ref(false)
const outlineContent = ref('')
const pptExists = ref(false)

let websocket: WebSocket | null = null
let heartbeatInterval: ReturnType<typeof setInterval> | null = null

const filteredMessages = computed(() => messages.value.filter(msg => msg.agent_type !== 'proxy' && msg.type !== 'slide_complete'))

const inputPlaceholder = computed(() => {
  if (awaitingFeedback.value) return t('slideGen.feedbackPlaceholder')
  if (needInputPrompt.value) return t('slideGen.choicePlaceholder')
  return t('slideGen.defaultPlaceholder')
})

const stateLabel = computed(() => {
  if (connectionError.value && !isConnected.value) return t('slideGen.connectionError')
  if (isConnecting.value) return t('slideGen.connecting')
  if (!isConnected.value) return t('slideGen.waitingConnection')
  if (needInputPrompt.value) return t('slideGen.waitingInput')
  if (awaitingFeedback.value) return t('slideGen.waitingConfirm')
  if (isProcessing.value) return t('slideGen.generating')
  if (currentSlideId.value) return t('slideGen.completed')
  return t('slideGen.connected')
})

const stateTone = computed(() => {
  if (connectionError.value && !isConnected.value) return 'error'
  if (needInputPrompt.value || awaitingFeedback.value) return 'waiting'
  if (isProcessing.value || isConnecting.value) return 'active'
  if (currentSlideId.value) return 'complete'
  return 'ready'
})

const statusDescription = computed(() => {
  if (connectionError.value && !isConnected.value) return connectionError.value
  if (isConnecting.value) return t('slideGen.connectingDescription')
  if (needInputPrompt.value) return t('slideGen.needInputDescription')
  if (awaitingFeedback.value) return t('slideGen.awaitingFeedbackDescription')
  if (isProcessing.value) return t('slideGen.processingDescription')
  if (currentSlideId.value) return t('slideGen.completedDescription')
  return t('slideGen.connectedDescription')
})

const activeStageLabel = computed(() => {
  if (needInputPrompt.value) return t('slideGen.activeNeedInput')
  if (awaitingFeedback.value) return t('slideGen.activeAwaitingFeedback')
  if (isProcessing.value) return t('slideGen.activeProcessing')
  if (currentSlideId.value) return t('slideGen.activeReady')
  return t('slideGen.activeIdle')
})

const hasMessage = (keywords: string[]) => messages.value.some((msg) => {
  const haystack = `${msg.type} ${msg.agent_type || ''} ${msg.agent_name || ''} ${msg.content || ''}`
  return keywords.some(keyword => haystack.includes(keyword))
})

const stageIndex = computed(() => {
  if (!isConnected.value) return isConnecting.value ? 0 : -1
  if (currentSlideId.value) return 4
  if (hasMessage(['配图', '图片', 'image'])) return 3
  if (hasMessage(['writer', 'Writer', '撰写', '内容'])) return 2
  if (hasMessage(['大纲', 'outline', 'OUTLINE_PREVIEW']) || needInputPrompt.value) return 1
  return 0
})

const workflowSteps = computed(() => [
  { key: 'connect', label: t('slideGen.workflowConnect'), description: t('slideGen.workflowConnectDesc') },
  { key: 'outline', label: t('slideGen.workflowOutline'), description: t('slideGen.workflowOutlineDesc') },
  { key: 'content', label: t('slideGen.workflowContent'), description: t('slideGen.workflowContentDesc') },
  { key: 'images', label: t('slideGen.workflowImages'), description: t('slideGen.workflowImagesDesc') },
  { key: 'assets', label: t('slideGen.workflowAssets'), description: t('slideGen.workflowAssetsDesc') }
].map((step, index) => {
  let status = 'pending'
  if (stageIndex.value > index) status = 'done'
  if (stageIndex.value === index) status = isProcessing.value || isConnecting.value ? 'active' : 'done'
  if ((needInputPrompt.value || awaitingFeedback.value) && stageIndex.value === index) status = 'waiting'
  if (currentSlideId.value && index === 4 && !awaitingFeedback.value && !isProcessing.value) status = 'done'
  return { ...step, status }
}))

const formatTime = (timestamp?: string) => {
  const date = timestamp ? new Date(timestamp) : new Date()
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString(timeLocale.value, { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const connect = () => {
  if (isConnecting.value || isConnected.value) return
  if (!userId.value) {
    connectionError.value = t('slideGen.userMissing')
    return
  }

  connectionError.value = ''
  isConnecting.value = true

  const wsUrl = getSlideGenWsUrl(userId.value, activeLocale.value)
  websocket = new WebSocket(wsUrl)

  websocket.onopen = () => {
    isConnected.value = true
    isConnecting.value = false
    emit('connection-changed', true)

    heartbeatInterval = setInterval(() => {
      if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify({ type: 'ping', content: '', locale: activeLocale.value }))
      }
    }, 30000)
  }

  websocket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleMessage(data)
    } catch (error) {
      console.error('[WS] Error parsing message:', error)
    }
  }

  websocket.onclose = () => {
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }
    isConnected.value = false
    isConnecting.value = false
    emit('connection-changed', false)
    addSystemMessage(t('slideGen.disconnected'))
  }

  websocket.onerror = () => {
    isConnecting.value = false
    connectionError.value = t('slideGen.connectFailed')
    addSystemMessage(t('slideGen.connectError'))
  }
}

const disconnect = () => {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
    heartbeatInterval = null
  }
  if (websocket) {
    websocket.close()
    websocket = null
  }
  isConnected.value = false
  connectionError.value = ''
  emit('connection-changed', false)
  messages.value = []
  currentSlideId.value = ''
  awaitingFeedback.value = false
  needInputPrompt.value = ''
  isProcessing.value = false
  pptExists.value = false
  resourceImages.value = []
}

const handleMessage = (data: SlideGenMessage) => {
  if (data.type === 'agent_message' && data.content && data.content.includes('[OUTLINE_PREVIEW]')) {
    const match = data.content.match(/\[OUTLINE_PREVIEW\]([\s\S]*?)\[\/OUTLINE_PREVIEW\]/)
    if (match) {
      const outlineText = match[1]
      outlineContent.value = outlineText
      messages.value.push({
        type: 'outline_action',
        agent_name: data.agent_name,
        agent_type: data.agent_type,
        content: outlineText,
        timestamp: data.timestamp || new Date().toISOString()
      })
      scrollToBottom()
      return
    }
  }

  if (data.type !== 'slide_complete') {
    messages.value.push(data)
  }
  scrollToBottom()

  if (data.type === 'slide_complete') {
    currentSlideId.value = String(data.content || '')
    awaitingFeedback.value = true
    isProcessing.value = false
    pptExists.value = false
    resourceImages.value = []
    messages.value.push({
      type: 'slide_complete_action',
      content: String(data.content || ''),
      timestamp: new Date().toISOString()
    })
    scrollToBottom()
  } else if (data.type === 'status' && isResourceReadyStatus(data.content)) {
    awaitingFeedback.value = false
    isProcessing.value = !isFinalResourceStatus(data.content)
    void refreshResources(false)
  } else if (data.type === 'need_input') {
    isProcessing.value = false
    needInputPrompt.value = data.content || ''
  } else if (data.type === 'error') {
    isProcessing.value = false
  }
}

const addSystemMessage = (content: string) => {
  messages.value.push({
    type: 'status',
    content,
    timestamp: new Date().toISOString()
  })
  scrollToBottom()
}

const sendMessage = (content: string) => {
  if (!websocket || !content.trim()) return

  const messageType = awaitingFeedback.value ? 'feedback' : 'user_input'
  websocket.send(JSON.stringify({ type: messageType, content, locale: activeLocale.value }))
  isProcessing.value = true
  needInputPrompt.value = ''

  messages.value.push({
    type: 'user_input',
    agent_name: userName.value,
    agent_type: 'user',
    content,
    timestamp: new Date().toISOString()
  })
  scrollToBottom()

  if (awaitingFeedback.value) {
    awaitingFeedback.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const previewSlide = async () => {
  if (!currentSlideId.value || !userId.value) return

  try {
    const url = slideResourceUrl(userId.value, currentSlideId.value, '')
    const response = await fetch(url)
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    const data = await response.json()
    previewContent.value = data.content
    showPreviewModal.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    addSystemMessage(t('slideGen.fetchSlideFailed', { message }))
  }
}

const closePreview = () => {
  showPreviewModal.value = false
  previewContent.value = ''
}

const previewOutline = (content?: string) => {
  previewContent.value = content || outlineContent.value
  showPreviewModal.value = true
}

const previewBodyRef = ref<HTMLElement | null>(null)

const renderedPreviewContent = computed(() => {
  if (!previewContent.value) return ''
  return marked.parse(previewContent.value, { breaks: true, gfm: true })
})

watch(renderedPreviewContent, () => {
  nextTick(() => renderMermaidIn(previewBodyRef.value))
})

const refreshResources = async (showModal: boolean) => {
  if (!currentSlideId.value || !userId.value) return

  isLoadingResources.value = true
  if (showModal) showResourcesModal.value = true

  try {
    const imagesUrl = slideResourceUrl(userId.value, currentSlideId.value, '/images')
    const imagesResponse = await fetch(imagesUrl)
    if (imagesResponse.ok) {
      const data = await imagesResponse.json()
      resourceImages.value = normalizeSlideImages(data)
    } else {
      resourceImages.value = []
    }

    const pptUrl = slideResourceUrl(userId.value, currentSlideId.value, '/ppt/exists')
    const pptResponse = await fetch(pptUrl)
    if (pptResponse.ok) {
      const pptData = await pptResponse.json()
      pptExists.value = Boolean(pptData.exists)
    } else {
      pptExists.value = false
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    if (showModal) addSystemMessage(t('slideGen.fetchResourceFailed', { message }))
    resourceImages.value = []
  } finally {
    isLoadingResources.value = false
  }
}

const openResources = async () => {
  await refreshResources(true)
}

const closeResources = () => {
  showResourcesModal.value = false
}

const getImageUrl = (imageId: string | number) => slideResourceUrl(userId.value, currentSlideId.value, `/images/${imageId}`)

const downloadMarkdown = () => {
  window.open(slideResourceUrl(userId.value, currentSlideId.value, '/download/markdown'), '_blank')
}

const downloadPPT = () => {
  window.open(slideResourceUrl(userId.value, currentSlideId.value, '/download/ppt'), '_blank')
}

const downloadImage = (imageId: string | number) => {
  window.open(slideResourceUrl(userId.value, currentSlideId.value, `/download/image/${imageId}`), '_blank')
}

const downloadAllImages = () => {
  resourceImages.value.forEach((img, index) => {
    setTimeout(() => downloadImage(img.image_id), index * 300)
  })
}

const previewMarkdownFromResources = async () => {
  await previewSlide()
}

onUnmounted(() => {
  if (heartbeatInterval) clearInterval(heartbeatInterval)
  if (websocket) websocket.close()
})
</script>

<style scoped>
.slidegen-workspace {
  height: 100%;
  color: #111827;
}

.connection-panel {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1rem, 4vw, 2.5rem);
}

.connection-card {
  position: relative;
  display: grid;
  gap: 1.1rem;
  width: min(640px, 100%);
  overflow: hidden;
  padding: clamp(1.35rem, 3vw, 2rem);
  border: 1px solid rgba(99, 102, 241, 0.14);
  background:
    radial-gradient(circle at top right, rgba(99, 102, 241, 0.14), transparent 38%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.94));
  box-shadow: 0 28px 80px rgba(55, 48, 163, 0.18);
  animation: panelIn 260ms ease-out;
}

.connection-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 4px;
  background: linear-gradient(90deg, #722ed1, #06b6d4, #10b981);
}

.slide-id,
.pending-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  min-height: 30px;
  padding: 0.32rem 0.72rem;
  border-radius: 999px;
  border: 1px solid rgba(114, 46, 209, 0.16);
  background: rgba(114, 46, 209, 0.08);
  color: #5b21b6;
  font-size: 0.78rem;
  font-weight: 700;
  white-space: nowrap;
}

.connection-copy {
  position: relative;
  padding: 0.15rem 0 0.95rem;
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
}

.connection-copy h1 {
  margin: 0.2rem 0 0.6rem;
  color: #111827;
  font-size: clamp(1.85rem, 3vw, 2.55rem);
  line-height: 1.15;
  letter-spacing: 0;
}

.connection-copy p:not(.eyebrow) {
  margin: 0;
  color: #596275;
  font-size: 0.98rem;
  line-height: 1.7;
}

.eyebrow {
  margin: 0;
  color: #722ed1;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.connection-details {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0.8rem;
}

.user-preview {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  min-width: 0;
  margin: 0;
  padding: 0.9rem;
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.14);
  background: rgba(255, 255, 255, 0.72);
}

.preview-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  object-fit: cover;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #722ed1, #06b6d4);
  color: #fff;
  font-weight: 800;
}

.preview-label {
  display: block;
  color: #697386;
  font-size: 0.75rem;
  line-height: 1.4;
}

.user-preview strong {
  color: #111827;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  min-height: 100%;
  margin: 0;
  padding: 0.8rem 0.9rem;
  border-radius: 12px;
  border: 1px solid rgba(79, 70, 229, 0.14);
  background: rgba(79, 70, 229, 0.07);
  color: #4338ca;
  font-size: 0.86rem;
}

.connection-status.error {
  border-color: rgba(220, 38, 38, 0.22);
  background: #fff1f2;
  color: #b91c1c;
}

.status-dot,
.live-indicator {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #6366f1;
  box-shadow: 0 0 0 5px rgba(99, 102, 241, 0.12);
  flex-shrink: 0;
}

.connection-status.error .status-dot,
.live-indicator.error {
  background: #dc2626;
  box-shadow: 0 0 0 5px rgba(220, 38, 38, 0.12);
}

.connection-status.connecting .status-dot,
.live-indicator.active {
  background: #0891b2;
  box-shadow: 0 0 0 5px rgba(8, 145, 178, 0.12);
}

.live-indicator.waiting {
  background: #d97706;
  box-shadow: 0 0 0 5px rgba(217, 119, 6, 0.14);
}

.live-indicator.complete {
  background: #059669;
  box-shadow: 0 0 0 5px rgba(5, 150, 105, 0.14);
}

.connect-button,
.task-button,
.panel-primary-button,
.status-action,
.resource-action,
.image-download {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  border: 0;
  cursor: pointer;
  font-weight: 800;
  transition: transform 180ms ease, box-shadow 180ms ease, background-color 180ms ease, border-color 180ms ease, color 180ms ease, opacity 180ms ease;
}

.connect-button {
  width: 100%;
  min-height: 46px;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: linear-gradient(135deg, #722ed1 0%, #8b47ea 100%);
  color: #fff;
  box-shadow: 0 18px 36px rgba(114, 46, 209, 0.28);
}

@media (min-width: 640px) {
  .connection-details {
    grid-template-columns: minmax(220px, 0.82fr) minmax(0, 1fr);
    align-items: stretch;
  }
}

.connect-button:hover:not(:disabled),
.task-button:hover:not(:disabled),
.panel-primary-button:hover:not(:disabled),
.status-action:hover:not(:disabled),
.resource-action:hover:not(:disabled),
.image-download:hover:not(:disabled) {
  transform: translateY(-1px);
}

.connect-button:hover:not(:disabled) {
  box-shadow: 0 22px 44px rgba(114, 46, 209, 0.34);
}

.connect-button:disabled,
.task-button:disabled,
.panel-primary-button:disabled,
.status-action:disabled,
.resource-action:disabled {
  opacity: 0.48;
  cursor: not-allowed;
  box-shadow: none;
}

.connect-button:focus-visible,
.task-button:focus-visible,
.panel-primary-button:focus-visible,
.status-action:focus-visible,
.resource-action:focus-visible,
.image-download:focus-visible,
.close-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.34);
  border-radius: 999px;
  border-top-color: #fff;
  animation: spin 800ms linear infinite;
}

.loading-spinner.dark {
  border-color: rgba(114, 46, 209, 0.18);
  border-top-color: #722ed1;
}

.workspace-grid {
  height: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 340px);
  grid-template-rows: auto minmax(0, 1fr);
  gap: 1rem;
  padding: 1rem;
}

.task-status-bar {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  min-height: 78px;
  padding: 0.85rem 1rem;
  border: 1px solid rgba(114, 46, 209, 0.12);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 50px rgba(55, 48, 163, 0.12);
  backdrop-filter: blur(18px);
}

.task-status-main,
.conversation-title,
.panel-section-header,
.section-title,
.status-card-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.task-status-main h2,
.conversation-title h3,
.panel-section h3,
.modal-header h3,
.section-title h4 {
  margin: 0;
  color: #111827;
  letter-spacing: 0;
}

.task-status-main h2 {
  font-size: 1.08rem;
}

.task-status-main span:not(.live-indicator),
.conversation-title p,
.panel-section-header p,
.state-panel p {
  color: #667085;
  font-size: 0.82rem;
  line-height: 1.45;
}

.task-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 0.55rem;
}

.task-button {
  min-height: 36px;
  padding: 0.46rem 0.78rem;
  border: 1px solid rgba(114, 46, 209, 0.18);
  border-radius: 999px;
  background: #fff;
  color: #5b21b6;
  font-size: 0.82rem;
}

.task-button.secondary {
  color: #475569;
  border-color: rgba(15, 23, 42, 0.1);
}

.conversation-panel,
.resource-panel {
  min-height: 0;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 20px 58px rgba(55, 48, 163, 0.12);
}

.conversation-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1rem 0.8rem;
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
}

.panel-icon {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.message-count {
  color: #697386;
  font-size: 0.8rem;
  white-space: nowrap;
}

.messages-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 1rem;
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.7), rgba(255, 255, 255, 0.72)),
    radial-gradient(circle at 18% 0%, rgba(114, 46, 209, 0.08), transparent 28%);
}

.empty-chat {
  display: grid;
  place-items: center;
  min-height: 260px;
  padding: 2rem;
  text-align: center;
  color: #667085;
}

.empty-chat .iconify {
  width: 52px;
  height: 52px;
  margin-bottom: 0.75rem;
  color: #722ed1;
}

.empty-chat h3 {
  margin: 0 0 0.35rem;
  color: #111827;
  font-size: 1.06rem;
}

.empty-chat p {
  max-width: 420px;
  margin: 0;
  line-height: 1.65;
}

.status-card {
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.14);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 34px rgba(30, 41, 59, 0.08);
  animation: panelIn 240ms ease-out;
}

.status-card.complete {
  border-color: rgba(5, 150, 105, 0.2);
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.96), rgba(255, 255, 255, 0.96));
}

.status-card.outline {
  border-color: rgba(79, 70, 229, 0.2);
  background: linear-gradient(135deg, rgba(238, 242, 255, 0.96), rgba(255, 255, 255, 0.96));
}

.status-card.waiting {
  border-color: rgba(217, 119, 6, 0.22);
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.96), rgba(255, 255, 255, 0.96));
}

.status-card-content {
  align-items: flex-start;
}

.status-card-icon,
.status-avatar {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
  font-size: 1.35rem;
  flex-shrink: 0;
}

.status-card.complete .status-card-icon {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}

.status-card.waiting .status-card-icon {
  background: rgba(217, 119, 6, 0.12);
  color: #b45309;
}

.status-avatar {
  object-fit: cover;
  background: #fff;
}

.status-card-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.25rem;
}

.status-card strong {
  color: #111827;
}

.status-card time {
  color: #8a8fa3;
  font-size: 0.75rem;
}

.status-card p {
  margin: 0;
  color: #536070;
  line-height: 1.65;
}

.status-action {
  min-height: 36px;
  margin-top: 0.85rem;
  padding: 0.5rem 0.82rem;
  border-radius: 999px;
  background: #fff;
  color: #5b21b6;
  border: 1px solid rgba(114, 46, 209, 0.18);
}

.status-action.success {
  background: linear-gradient(135deg, #059669, #10b981);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 14px 28px rgba(5, 150, 105, 0.2);
}

.resource-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
  overflow: auto;
}

.panel-section {
  padding: 0.95rem;
  border: 1px solid rgba(99, 102, 241, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
}

.timeline {
  display: grid;
  gap: 0.75rem;
  margin: 1rem 0 0;
  padding: 0;
  list-style: none;
}

.timeline li {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  gap: 0.65rem;
  align-items: flex-start;
  color: #697386;
}

.timeline-dot {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  background: #fff;
  color: #9ca3af;
}

.timeline li.done .timeline-dot,
.timeline li.active .timeline-dot,
.timeline li.waiting .timeline-dot {
  border-color: transparent;
  color: #fff;
}

.timeline li.done .timeline-dot {
  background: #059669;
}

.timeline li.active .timeline-dot {
  background: #722ed1;
}

.timeline li.waiting .timeline-dot {
  background: #d97706;
}

.timeline strong {
  display: block;
  color: #1f2937;
  font-size: 0.88rem;
}

.timeline p {
  margin: 0.18rem 0 0;
  color: #697386;
  font-size: 0.78rem;
  line-height: 1.45;
}

.panel-primary-button {
  width: 100%;
  min-height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  box-shadow: 0 14px 28px rgba(114, 46, 209, 0.22);
}

.state-panel {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
  border-color: rgba(79, 70, 229, 0.16);
  background: rgba(79, 70, 229, 0.06);
}

.state-panel .iconify {
  color: #4f46e5;
  font-size: 1.3rem;
  flex-shrink: 0;
}

.state-panel.complete {
  border-color: rgba(5, 150, 105, 0.18);
  background: rgba(5, 150, 105, 0.08);
}

.state-panel.complete .iconify {
  color: #059669;
}

.state-panel.waiting {
  border-color: rgba(217, 119, 6, 0.2);
  background: rgba(217, 119, 6, 0.08);
}

.state-panel.waiting .iconify {
  color: #b45309;
}

.state-panel strong {
  color: #111827;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(8px);
}

.preview-modal,
.resources-modal {
  width: min(940px, 100%);
  max-height: min(86vh, 900px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: #fff;
  box-shadow: 0 32px 90px rgba(15, 23, 42, 0.32);
  animation: modalIn 240ms ease-out;
}

.resources-modal {
  width: min(900px, 100%);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.2rem;
  border-bottom: 1px solid rgba(99, 102, 241, 0.13);
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.1), rgba(6, 182, 212, 0.08));
}

.modal-header.resources {
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.1), rgba(16, 185, 129, 0.08));
}

.close-button {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #334155;
  cursor: pointer;
  transition: background-color 180ms ease, color 180ms ease;
}

.close-button:hover {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.preview-body,
.resources-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 1.25rem;
}

.preview-body {
  color: #273142;
  font-size: 1rem;
  line-height: 1.8;
}

.preview-body :deep(h1),
.preview-body :deep(h2),
.preview-body :deep(h3) {
  color: #111827;
  line-height: 1.35;
}

.preview-body :deep(h1) {
  margin: 1.2rem 0 0.8rem;
  padding-bottom: 0.55rem;
  border-bottom: 2px solid rgba(114, 46, 209, 0.2);
  font-size: 1.65rem;
}

.preview-body :deep(h2) {
  margin: 1.1rem 0 0.7rem;
  font-size: 1.32rem;
}

.preview-body :deep(h3) {
  margin: 1rem 0 0.5rem;
  font-size: 1.08rem;
}

.preview-body :deep(p) {
  margin-bottom: 1rem;
}

.preview-body :deep(ul),
.preview-body :deep(ol) {
  margin: 0.8rem 0;
  padding-left: 1.5rem;
}

.preview-body :deep(code) {
  padding: 0.16rem 0.36rem;
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.07);
}

.preview-body :deep(pre) {
  padding: 1rem;
  overflow-x: auto;
  border-radius: 12px;
  background: #111827;
  color: #f8fafc;
}

.preview-body :deep(blockquote) {
  margin: 1rem 0;
  padding: 0.65rem 1rem;
  border-left: 4px solid #722ed1;
  border-radius: 0 10px 10px 0;
  background: rgba(114, 46, 209, 0.06);
  color: #4b5563;
}

.preview-body :deep(table) {
  width: 100%;
  margin: 1rem 0;
  border-collapse: collapse;
}

.preview-body :deep(th),
.preview-body :deep(td) {
  padding: 0.7rem;
  border: 1px solid rgba(15, 23, 42, 0.12);
  text-align: left;
}

.preview-body :deep(th) {
  background: rgba(114, 46, 209, 0.06);
}

.resources-body {
  display: grid;
  gap: 1rem;
  background: #f8fafc;
}

.resource-section {
  padding: 1rem;
  border: 1px solid rgba(99, 102, 241, 0.12);
  border-radius: 16px;
  background: #fff;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.section-icon {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
  font-size: 1.35rem;
}

.section-icon.indigo {
  background: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
}

.section-icon.cyan {
  background: rgba(6, 182, 212, 0.11);
  color: #0891b2;
}

.section-title h4 {
  font-size: 1rem;
}

.file-name {
  margin: 0.15rem 0 0;
  color: #697386;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.78rem;
}

.section-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.5rem;
}

.resource-action {
  min-height: 34px;
  padding: 0.42rem 0.72rem;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  color: #475569;
  font-size: 0.82rem;
}

.resource-action.primary {
  border-color: transparent;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
}

.pending-pill {
  min-height: 28px;
  color: #697386;
  background: rgba(15, 23, 42, 0.05);
  border-color: rgba(15, 23, 42, 0.08);
}

.image-section .section-header {
  align-items: flex-start;
}

.loading-state,
.empty-state {
  display: grid;
  place-items: center;
  gap: 0.5rem;
  min-height: 150px;
  color: #697386;
}

.empty-state .iconify {
  width: 42px;
  height: 42px;
  color: #94a3b8;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 0.9rem;
  margin-top: 1rem;
}

.image-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.image-wrapper {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 0.25rem;
  padding: 0.75rem;
  background: linear-gradient(180deg, transparent 0%, rgba(15, 23, 42, 0.82) 100%);
  color: #fff;
  opacity: 0;
  transition: opacity 180ms ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-overlay p {
  margin: 0;
  font-size: 0.74rem;
  line-height: 1.35;
  word-break: break-word;
}

.image-overlay strong {
  margin-right: 0.4rem;
  color: #a5f3fc;
}

.image-download {
  position: absolute;
  right: 0.55rem;
  top: 0.55rem;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #5b21b6;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.18);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes panelIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 1120px) {
  .workspace-grid {
    grid-template-columns: 1fr;
    overflow-y: auto;
  }

  .resource-panel {
    overflow: visible;
  }
}

@media (max-width: 760px) {
  .connection-panel,
  .workspace-grid {
    padding: 0.75rem;
  }

  .task-status-bar,
  .section-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .task-actions,
  .section-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .task-button,
  .slide-id {
    width: 100%;
    justify-content: center;
  }

  .conversation-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .modal-overlay {
    padding: 0.75rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .slidegen-workspace,
  .slidegen-workspace * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
