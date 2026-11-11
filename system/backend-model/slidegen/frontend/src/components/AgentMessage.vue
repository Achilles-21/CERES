<template>
  <div class="message-wrapper" :class="messageClass">
    <div class="message-bubble">
      <div class="avatar-container" v-if="avatarSrc">
        <img :src="avatarSrc" :alt="displayName" class="avatar-img" />
      </div>
      <div v-else class="avatar-placeholder" :style="{ backgroundColor: agentColor }">
        {{ avatarText }}
      </div>

      <div class="message-content">
        <div class="message-header">
          <span class="agent-name">{{ displayName }}</span>
          <span class="timestamp">{{ formattedTime }}</span>
        </div>
        <div class="message-body" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  agentName: {
    type: String,
    default: ''
  },
  agentType: {
    type: String,
    default: ''
  },
  content: {
    type: String,
    required: true
  },
  timestamp: {
    type: String,
    required: true
  },
  messageType: {
    type: String,
    default: 'agent_message'
  },
  userAvatar: {
    type: String,
    default: ''
  }
})

const agentConfigs = {
  proxy: { color: '#2196F3', name: '教师代理', avatar: 'P' },
  designer: { color: '#4CAF50', name: '大纲设计', avatar: 'D' },
  reviewer: { color: '#FF9800', name: '审核员', avatar: 'R' },
  planner: { color: '#9C27B0', name: '规划员', avatar: 'L' },
  writer: { color: '#00BCD4', name: '撰写员', avatar: 'W' },
  user: { color: '#607D8B', name: '用户', avatar: 'U' },
  status: { color: '#9E9E9E', name: '系统', avatar: 'S' },
  error: { color: '#F44336', name: '错误', avatar: '!' }
}

const agentAvatarPaths = {
  designer: '/avatars/DesignerAgent.svg',
  reviewer: '/avatars/ReviewerAgent.svg',
  planner: '/avatars/PlannerAgent.svg'
}

const config = computed(() => {
  if (props.messageType === 'status') return agentConfigs.status
  if (props.messageType === 'error') return agentConfigs.error
  if (props.messageType === 'user_input') return agentConfigs.user
  return agentConfigs[props.agentType] || agentConfigs.status
})

const agentColor = computed(() => config.value.color)

const avatarText = computed(() => config.value.avatar)

const avatarSrc = computed(() => {
  if (props.messageType === 'user_input' && props.userAvatar) {
    return props.userAvatar
  }

  if (props.messageType === 'status') {
    return '/avatars/System.svg'
  }

  if (props.messageType === 'error') {
    return null
  }

  if (props.agentType === 'writer') {
    const match = props.agentName?.match(/writer[_\s]*(\d)/i)
    if (match) {
      const writerNum = match[1]
      return `/avatars/WriterAgent${writerNum}.svg`
    }
    return '/avatars/WriterAgent1.svg'  
  }

  return agentAvatarPaths[props.agentType] || null
})

const displayName = computed(() => {
  if (props.agentName) return props.agentName
  return config.value.name
})

const messageClass = computed(() => ({
  'is-user': props.messageType === 'user_input',
  'is-status': props.messageType === 'status',
  'is-error': props.messageType === 'error'
}))

const formattedTime = computed(() => {
  const date = new Date(props.timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

const renderedContent = computed(() => {
  return marked.parse(props.content, {
    breaks: true,
    gfm: true
  })
})
</script>

<style scoped>
.message-wrapper {
  margin-bottom: 1rem;
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

.message-bubble {
  display: flex;
  gap: 0.75rem;
  max-width: 85%;
}

.is-user .message-bubble {
  flex-direction: row-reverse;
  margin-left: auto;
}

.is-status .message-bubble {
  max-width: 100%;
  justify-content: center;
  margin: 0 auto;
}

.avatar-container {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease;
}

.avatar-container:hover {
  transform: scale(1.05);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.is-status .avatar-container,
.is-status .avatar-placeholder {
  width: 36px;
  height: 36px;
  font-size: 0.9rem;
}

.message-content {
  background: white;
  border-radius: 12px;
  padding: 0.75rem 1rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.2s ease;
}

.message-content:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.is-user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.is-status .message-content {
  background: #f5f5f5;
  color: #666;
  font-size: 0.9rem;
}

.is-error .message-content {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.agent-name {
  font-weight: 600;
  font-size: 0.85rem;
}

.is-user .agent-name {
  color: rgba(255, 255, 255, 0.9);
}

.timestamp {
  font-size: 0.75rem;
  color: #999;
}

.is-user .timestamp {
  color: rgba(255, 255, 255, 0.7);
}

.message-body {
  font-size: 0.95rem;
  line-height: 1.6;
}

.message-body :deep(h1),
.message-body :deep(h2),
.message-body :deep(h3) {
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.message-body :deep(h1) {
  font-size: 1.3rem;
}

.message-body :deep(h2) {
  font-size: 1.15rem;
}

.message-body :deep(h3) {
  font-size: 1rem;
}

.message-body :deep(ul),
.message-body :deep(ol) {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.message-body :deep(li) {
  margin-bottom: 0.25rem;
}

.message-body :deep(p) {
  margin-bottom: 0.5rem;
}

.message-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-body :deep(code) {
  background: #f5f5f5;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  font-size: 0.9em;
}

.is-user .message-body :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-body :deep(pre) {
  background: #f5f5f5;
  padding: 0.75rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.5rem 0;
}

.message-body :deep(pre code) {
  background: none;
  padding: 0;
}
</style>
