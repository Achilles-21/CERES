<template>
  <div class="chat-room">
    <div v-if="!isConnected" class="connection-panel">
      <div class="connection-card">
        <img src="/avatars/logo.svg" alt="Ceres Logo" class="connection-logo" />

        <div class="avatar-selection">
          <label class="section-label">Step 1: 头像</label>
          <div class="avatar-grid">
            <div
              v-for="i in 6"
              :key="i"
              class="avatar-option"
              :class="{ selected: selectedAvatar === i }"
              @click="selectAvatar(i)"
            >
              <img :src="`/avatars/User${i}.svg`" :alt="`User ${i}`" />
            </div>
          </div>
        </div>

        <div class="input-group">
          <label for="userId">Step 2: 用户 ID</label>
          <input
            id="userId"
            v-model="userId"
            type="text"
            placeholder="输入您的用户 ID"
            @keyup.enter="connect"
          />
        </div>
        <button
          class="btn btn-primary"
          @click="connect"
          :disabled="!userId.trim() || !selectedAvatar"
        >
          <span v-if="isConnecting" class="loading-spinner"></span>
          {{ isConnecting ? '连接中...' : '连接' }}
        </button>
        <p v-if="!selectedAvatar && userId.trim()" class="hint-text">请选择一个头像</p>
      </div>
    </div>

    <div v-else class="chat-interface">
      <div class="chat-header">
        <div class="header-info">
          <div class="user-info">
            <img :src="userAvatarPath" alt="User Avatar" class="header-avatar" />
            <span class="user-name">{{ userId }}</span>
          </div>
          <span v-if="currentSlideId" class="slide-badge">Slide ID: {{ currentSlideId }}</span>
          <button v-if="currentSlideId" class="btn btn-small btn-resources" @click="openResources">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
              <path d="M20 6h-8l-2-2H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm0 12H4V8h16v10z"/>
            </svg>
            资源
          </button>
        </div>
        <button class="btn btn-small btn-secondary" @click="disconnect">断开连接</button>
      </div>

      <div class="messages-area" ref="messagesContainer">
        <template v-for="(msg, index) in filteredMessages" :key="index">
          <div v-if="msg.type === 'slide_complete_action'" class="slide-complete-card">
            <div class="slide-complete-content">
              <img src="/avatars/System.svg" alt="System" class="system-avatar" />
              <div class="slide-complete-info">
                <p>课件生成完成！点击下方按钮预览内容。</p>
                <p class="slide-complete-hint">如满意请输入"确认"或"满意"，如有修改意见请直接输入。</p>
              </div>
            </div>
            <button class="btn btn-preview" @click="previewSlide">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
              </svg>
              预览课件
            </button>
          </div>
          <div v-else-if="msg.type === 'outline_action'" class="outline-action-card">
            <div class="outline-action-content">
              <img src="/avatars/DesignerAgent.svg" alt="Designer" class="agent-avatar-small" />
              <div class="outline-action-info">
                <span class="agent-label">DesignerAgent</span>
                <p>大纲设计完成！点击预览查看详情。</p>
              </div>
            </div>
            <button class="btn btn-outline-preview" @click="previewOutline(msg.content)">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
              </svg>
              预览大纲
            </button>
          </div>
          <div v-else-if="msg.type === 'need_input'" class="need-input-card">
            <div class="need-input-content">
              <img src="/avatars/DesignerAgent.svg" alt="Designer" class="agent-avatar-small" />
              <div class="need-input-body">
                <span class="agent-label">DesignerAgent</span>
                <p class="need-input-text">{{ msg.content }}</p>
              </div>
            </div>
          </div>
          <AgentMessage
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

      <UserInput
        :disabled="isProcessing"
        :placeholder="inputPlaceholder"
        @send="sendMessage"
      />
    </div>

    <div v-if="showPreviewModal" class="preview-modal-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <div class="preview-header">
          <h3>课件预览</h3>
          <button class="close-btn" @click="closePreview">×</button>
        </div>
        <div class="preview-body" v-html="renderedPreviewContent"></div>
      </div>
    </div>

    <div v-if="showResourcesModal" class="preview-modal-overlay" @click.self="closeResources">
      <div class="resources-modal">
        <div class="resources-header">
          <h3>课件资源</h3>
          <button class="close-btn" @click="closeResources">×</button>
        </div>
        <div class="resources-body">
          <div class="resource-section">
            <div class="section-header">
              <h4>📄 课件文档</h4>
              <div class="section-actions">
                <button class="btn btn-small btn-action" @click="previewMarkdownFromResources">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                  </svg>
                  预览
                </button>
                <button class="btn btn-small btn-action" @click="downloadMarkdown">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                    <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                  </svg>
                  下载
                </button>
              </div>
            </div>
            <p class="file-name">slide_draft.md</p>
          </div>

          <div class="resource-section">
            <div class="section-header">
              <h4>📊 PPT 演示文稿</h4>
              <div class="section-actions">
                <button
                  v-if="pptExists"
                  class="btn btn-small btn-action btn-ppt"
                  @click="downloadPPT"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                    <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                  </svg>
                  下载 PPT
                </button>
                <span v-else class="ppt-status">暂未生成</span>
              </div>
            </div>
            <p class="file-name">slide.pptx</p>
          </div>

          <div class="resource-section">
            <div class="section-header">
              <h4>🖼️ 配图资源 ({{ resourceImages.length }}张)</h4>
              <button
                v-if="resourceImages.length > 0"
                class="btn btn-small btn-action"
                @click="downloadAllImages"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                  <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                </svg>
                全部下载
              </button>
            </div>

            <div v-if="isLoadingResources" class="loading-state">
              <span class="loading-spinner"></span> 加载中...
            </div>

            <div v-else-if="resourceImages.length === 0" class="empty-state">
              暂无配图
            </div>

            <div v-else class="images-grid">
              <div
                v-for="img in resourceImages"
                :key="img.image_id"
                class="image-card"
              >
                <div class="image-wrapper">
                  <img :src="getImageUrl(img.image_id)" :alt="`Image ${img.image_id}`" />
                  <div class="image-overlay">
                    <div class="image-info">
                      <p><strong>ID:</strong> {{ img.image_id }}</p>
                      <p><strong>位置:</strong> {{ img.position }}</p>
                      <p><strong>查询:</strong> {{ img.query }}</p>
                    </div>
                  </div>
                </div>
                <div class="image-actions">
                  <button class="btn-icon" @click="downloadImage(img.image_id)" title="下载">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                      <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted, computed } from 'vue'
import { marked } from 'marked'
import AgentMessage from './AgentMessage.vue'
import UserInput from './UserInput.vue'

const emit = defineEmits(['connection-changed'])

const userId = ref('')
const selectedAvatar = ref(null)
const isConnected = ref(false)
const isConnecting = ref(false)
const isProcessing = ref(false)
const needInputPrompt = ref('')       
const messages = ref([])
const currentSlideId = ref('')
const showApprovalPanel = ref(false)
const awaitingFeedback = ref(false)   
const messagesContainer = ref(null)
const showPreviewModal = ref(false)   
const previewContent = ref('')        
const showResourcesModal = ref(false)  
const resourceImages = ref([])         
const isLoadingResources = ref(false)  
const outlineContent = ref('')         
const pptExists = ref(false)           

let websocket = null
let heartbeatInterval = null

const userAvatarPath = computed(() => {
  return selectedAvatar.value ? `/avatars/User${selectedAvatar.value}.svg` : ''
})

const filteredMessages = computed(() => {
  return messages.value.filter(msg =>
    msg.agent_type !== 'proxy' && msg.type !== 'slide_complete'
  )
})

const inputPlaceholder = computed(() => {
  if (awaitingFeedback.value) return '请输入您的反馈（满意请输入"确认"，修改意见直接输入）'
  if (needInputPrompt.value) return '请在此输入您的选择...'
  return '输入您的课件需求...'
})

const selectAvatar = (index) => {
  selectedAvatar.value = index
}

const getApiBase = () => (import.meta.env.VITE_SLIDEGEN_API_URL || '/api').replace(/\/$/, '')

const getWsUrl = (userId) => {
  const explicit = import.meta.env.VITE_SLIDEGEN_WS_URL
  const url = new URL(explicit || '/ws', window.location.href)
  url.protocol = url.protocol === 'https:' || url.protocol === 'wss:' ? 'wss:' : 'ws:'
  return `${url.toString().replace(/\/$/, '')}/${encodeURIComponent(userId)}`
}

const connect = () => {
  if (!userId.value.trim() || !selectedAvatar.value) return

  isConnecting.value = true

  const wsUrl = getWsUrl(userId.value)
  console.log('[WS] Connecting to:', wsUrl)
  websocket = new WebSocket(wsUrl)

  websocket.onopen = () => {
    console.log('[WS] Connection opened')
    isConnected.value = true
    isConnecting.value = false
    emit('connection-changed', true)

    heartbeatInterval = setInterval(() => {
      if (websocket && websocket.readyState === WebSocket.OPEN) {
        console.log('[WS] Sending heartbeat ping')
        websocket.send(JSON.stringify({ type: 'ping', content: '' }))
      }
    }, 30000)
  }

  websocket.onmessage = (event) => {
    console.log('[WS] Message received:', event.data)
    try {
      const data = JSON.parse(event.data)
      handleMessage(data)
    } catch (e) {
      console.error('[WS] Error parsing message:', e)
    }
  }

  websocket.onclose = (event) => {
    console.log('[WS] Connection closed. Code:', event.code, 'Reason:', event.reason, 'Clean:', event.wasClean)
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }
    isConnected.value = false
    isConnecting.value = false
    emit('connection-changed', false)
    addSystemMessage('连接已断开')
  }

  websocket.onerror = (error) => {
    console.error('[WS] WebSocket error:', error)
    isConnecting.value = false
    addSystemMessage('连接错误')
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
  emit('connection-changed', false)
  messages.value = []
  currentSlideId.value = ''
  showApprovalPanel.value = false
  awaitingFeedback.value = false
}

const handleMessage = (data) => {
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
    currentSlideId.value = data.content
    awaitingFeedback.value = true     
    isProcessing.value = false        
    showApprovalPanel.value = false   
    messages.value.push({
      type: 'slide_complete_action',
      content: data.content,
      timestamp: new Date().toISOString()
    })
    scrollToBottom()
  } else if (data.type === 'status' && data.content.includes('配图匹配完成')) {
    showApprovalPanel.value = false
    awaitingFeedback.value = false    
    isProcessing.value = false
  } else if (data.type === 'need_input') {
    isProcessing.value = false
    needInputPrompt.value = data.content || ''
  } else if (data.type === 'error') {
    isProcessing.value = false
  }
}

const addSystemMessage = (content) => {
  messages.value.push({
    type: 'status',
    content,
    timestamp: new Date().toISOString()
  })
  scrollToBottom()
}

const sendMessage = (content) => {
  if (!websocket || !content.trim()) return

  const messageType = awaitingFeedback.value ? 'feedback' : 'user_input'

  const message = {
    type: messageType,
    content: content
  }

  websocket.send(JSON.stringify(message))
  isProcessing.value = true
  needInputPrompt.value = ''   

  messages.value.push({
    type: 'user_input',
    agent_name: userId.value,
    agent_type: 'user',
    content: content,
    timestamp: new Date().toISOString()
  })
  scrollToBottom()

  if (awaitingFeedback.value) {
    awaitingFeedback.value = false
  }
}

const approveSlide = () => {
  if (!websocket || !currentSlideId.value) return

  const message = {
    type: 'approval',
    content: currentSlideId.value
  }

  websocket.send(JSON.stringify(message))
  showApprovalPanel.value = false
  isProcessing.value = true
  addSystemMessage('正在配图...')
}

const skipImages = () => {
  showApprovalPanel.value = false
  addSystemMessage('已跳过配图')
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
    const apiBase = getApiBase()
    const url = `${apiBase}/slides/${userId.value}/${currentSlideId.value}`
    console.log('[API] Fetching slide:', url)

    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
    const data = await response.json()
    previewContent.value = data.content
    showPreviewModal.value = true
  } catch (error) {
    console.error('[API Error] Failed to fetch slide:', error)
    addSystemMessage(`获取课件内容失败: ${error.message}`)
  }
}

const closePreview = () => {
  showPreviewModal.value = false
  previewContent.value = ''
}

const previewOutline = (content) => {
  previewContent.value = content || outlineContent.value
  showPreviewModal.value = true
}

const renderedPreviewContent = computed(() => {
  if (!previewContent.value) return ''
  return marked.parse(previewContent.value, {
    breaks: true,
    gfm: true
  })
})

const openResources = async () => {
  if (!currentSlideId.value || !userId.value) return

  isLoadingResources.value = true
  showResourcesModal.value = true
  pptExists.value = false

  const apiBase = getApiBase()
  console.log('[API] Fetching resources from:', apiBase)

  try {
    const imagesUrl = `${apiBase}/slides/${userId.value}/${currentSlideId.value}/images`
    console.log('[API] Fetching images:', imagesUrl)
    const imagesResponse = await fetch(imagesUrl)
    if (imagesResponse.ok) {
      const data = await imagesResponse.json()
      resourceImages.value = data.images || []
    } else {
      console.warn('[API] Images not found:', imagesResponse.status)
      resourceImages.value = []
    }

    const pptUrl = `${apiBase}/slides/${userId.value}/${currentSlideId.value}/ppt/exists`
    console.log('[API] Checking PPT:', pptUrl)
    const pptResponse = await fetch(pptUrl)
    if (pptResponse.ok) {
      const pptData = await pptResponse.json()
      pptExists.value = pptData.exists
    }
  } catch (error) {
    console.error('[API Error] Failed to fetch resources:', error)
    addSystemMessage(`获取资源失败: ${error.message}`)
    resourceImages.value = []
  } finally {
    isLoadingResources.value = false
  }
}

const closeResources = () => {
  showResourcesModal.value = false
  resourceImages.value = []
}

const getImageUrl = (imageId) => {
  const apiBase = getApiBase()
  return `${apiBase}/slides/${userId.value}/${currentSlideId.value}/images/${imageId}`
}

const downloadMarkdown = () => {
  const apiBase = getApiBase()
  const url = `${apiBase}/slides/${userId.value}/${currentSlideId.value}/download/markdown`
  window.open(url, '_blank')
}

const downloadPPT = () => {
  const apiBase = getApiBase()
  const url = `${apiBase}/slides/${userId.value}/${currentSlideId.value}/download/ppt`
  window.open(url, '_blank')
}

const downloadImage = (imageId) => {
  const apiBase = getApiBase()
  const url = `${apiBase}/slides/${userId.value}/${currentSlideId.value}/download/image/${imageId}`
  window.open(url, '_blank')
}

const downloadAllImages = () => {
  resourceImages.value.forEach((img, index) => {
    setTimeout(() => {
      downloadImage(img.image_id)
    }, index * 300)  
  })
}

const previewMarkdownFromResources = async () => {
  await previewSlide()
}

onUnmounted(() => {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
  }
  if (websocket) {
    websocket.close()
  }
})
</script>

<style scoped>
.chat-room {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.connection-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.connection-card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  width: 100%;
  max-width: 450px;
  text-align: center;
}

.connection-logo {
  width: 350px;
  height: 55px;
  border-radius: 8px;
  object-fit: contain;
  margin-bottom: 1rem;
  box-shadow: none;
}

.connection-card h2 {
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.5rem;
}

.avatar-selection {
  margin-bottom: 1.5rem;
}

.section-label {
  display: block;
  margin-bottom: 0.75rem;
  color: #555;
  font-weight: 500;
  text-align: left;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.avatar-option {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s ease;
  background: #f5f5f5;
}

.avatar-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-option:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.avatar-option.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

.input-group {
  margin-bottom: 1.5rem;
  text-align: left;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.input-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.hint-text {
  margin-top: 0.75rem;
  font-size: 0.85rem;
  color: #ff9800;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.5rem;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.slide-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  background: #f3e5f5;
  color: #7b1fa2;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: #fafafa;
}

.approval-panel {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  text-align: center;
}

.approval-panel p {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.approval-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 100%;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #e0e0e0;
  color: #333;
}

.btn-secondary:hover:not(:disabled) {
  background: #d0d0d0;
}

.btn-small {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
}

.approval-panel .btn-primary {
  background: white;
  color: #667eea;
  width: auto;
}

.approval-panel .btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.slide-complete-card {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-radius: 12px;
  padding: 1.25rem;
  margin: 1rem auto;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-complete-content {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.system-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.slide-complete-info p {
  margin: 0;
  color: #2e7d32;
  font-weight: 500;
}

.slide-complete-hint {
  font-size: 0.85rem;
  color: #558b2f !important;
  margin-top: 0.5rem !important;
}

.btn-preview {
  width: 100%;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #4CAF50 0%, #2e7d32 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.btn-preview:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.need-input-card {
  background: linear-gradient(135deg, #ede7f6 0%, #d1c4e9 100%);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  margin: 0.75rem 0;
  max-width: 480px;
  box-shadow: 0 4px 12px rgba(103, 58, 183, 0.18);
  animation: fadeIn 0.3s ease-out;
  border-left: 4px solid #7c4dff;
}

.need-input-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.need-input-body {
  flex: 1;
}

.need-input-text {
  margin: 0.25rem 0 0;
  color: #4527a0;
  font-size: 0.95rem;
  line-height: 1.6;
  white-space: pre-wrap;
}

.outline-action-card {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 12px;
  padding: 1rem;
  margin: 0.75rem 0;
  max-width: 400px;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.2);
  animation: fadeIn 0.3s ease-out;
}

.outline-action-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.agent-avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.outline-action-info {
  flex: 1;
}

.agent-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #1565c0;
  display: block;
  margin-bottom: 0.25rem;
}

.outline-action-info p {
  margin: 0;
  color: #1976d2;
  font-size: 0.95rem;
}

.btn-outline-preview {
  width: 100%;
  padding: 0.6rem 1rem;
  background: linear-gradient(135deg, #2196F3 0%, #1565c0 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.btn-outline-preview:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
}

.preview-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.preview-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalIn 0.3s ease-out;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px 16px 0 0;
}

.preview-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: white;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 1.5rem;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.preview-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
  line-height: 1.8;
  font-size: 1rem;
}

.preview-body :deep(h1) {
  font-size: 1.75rem;
  color: #333;
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #667eea;
}

.preview-body :deep(h2) {
  font-size: 1.4rem;
  color: #444;
  margin-top: 1.25rem;
  margin-bottom: 0.75rem;
}

.preview-body :deep(h3) {
  font-size: 1.15rem;
  color: #555;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.preview-body :deep(p) {
  margin-bottom: 1rem;
  color: #333;
}

.preview-body :deep(ul),
.preview-body :deep(ol) {
  margin: 1rem 0;
  padding-left: 2rem;
}

.preview-body :deep(li) {
  margin-bottom: 0.5rem;
}

.preview-body :deep(code) {
  background: #f5f5f5;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-size: 0.9em;
}

.preview-body :deep(pre) {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1rem 0;
}

.preview-body :deep(blockquote) {
  border-left: 4px solid #667eea;
  margin: 1rem 0;
  padding: 0.5rem 1rem;
  background: #f8f9fa;
  color: #555;
}

.preview-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
}

.preview-body :deep(th),
.preview-body :deep(td) {
  border: 1px solid #ddd;
  padding: 0.75rem;
  text-align: left;
}

.preview-body :deep(th) {
  background: #f5f5f5;
  font-weight: 600;
}

.btn-resources {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  color: white;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.btn-resources:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.4);
}

.resources-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalIn 0.3s ease-out;
}

.resources-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border-radius: 16px 16px 0 0;
}

.resources-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: white;
}

.resources-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.resource-section {
  margin-bottom: 2rem;
}

.resource-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h4 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.section-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  background: #f5f5f5;
  color: #333;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.btn-action:hover {
  background: #e0e0e0;
}

.file-name {
  margin: 0;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  color: #555;
  font-family: monospace;
}

.btn-ppt {
  background: linear-gradient(135deg, #ff5722 0%, #e64a19 100%);
  color: white;
}

.btn-ppt:hover {
  background: linear-gradient(135deg, #e64a19 0%, #bf360c 100%);
}

.ppt-status {
  font-size: 0.85rem;
  color: #999;
  font-style: italic;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
}

.image-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.image-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-wrapper {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  opacity: 0;
  transition: opacity 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.image-info {
  color: white;
  font-size: 0.8rem;
  line-height: 1.5;
}

.image-info p {
  margin: 0.25rem 0;
  word-break: break-word;
}

.image-info strong {
  color: #ffc107;
}

.image-actions {
  display: flex;
  justify-content: center;
  padding: 0.5rem;
  background: #f8f9fa;
}

.btn-icon {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: #666;
  border-radius: 50%;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #e0e0e0;
  color: #333;
}
</style>
