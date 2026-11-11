<template>
  <article class="message-wrapper" :class="messageClass">
    <div class="avatar-shell" v-if="avatarSrc">
      <img :src="avatarSrc" :alt="displayName" class="avatar-img" />
    </div>
    <div v-else class="avatar-placeholder" :style="{ background: agentColor }">
      {{ avatarText }}
    </div>

    <div class="message-card ceres-card">
      <header class="message-header">
        <div class="agent-meta">
          <span class="agent-name">{{ displayName }}</span>
          <span class="role-badge">{{ roleLabel }}</span>
        </div>
        <time class="timestamp">{{ formattedTime }}</time>
      </header>
      <div ref="messageBodyRef" class="message-body" v-html="renderedContent"></div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { marked } from 'marked'
import '@/utils/mermaid'
import { renderMermaidIn } from '@/utils/mermaid'
import { slideGenAgentAvatar } from '@/utils/slideGenAvatars'
import i18n from '@/lang'

const props = defineProps({
  agentName: { type: String, default: '' },
  agentType: { type: String, default: '' },
  content: { type: String, required: true },
  timestamp: { type: String, default: '' },
  messageType: { type: String, default: 'agent_message' },
  userAvatar: { type: String, default: '' }
})

const t = (key: string) => i18n.global.t(key)

const agentConfigs: Record<string, { color: string; nameKey: string; avatar: string; role: string }> = {
  proxy: { color: '#2563eb', nameKey: 'slideGen.agents.proxy', avatar: 'P', role: 'Proxy' },
  designer: { color: '#722ed1', nameKey: 'slideGen.agents.designer', avatar: 'D', role: 'Designer' },
  reviewer: { color: '#059669', nameKey: 'slideGen.agents.reviewer', avatar: 'R', role: 'Reviewer' },
  planner: { color: '#4f46e5', nameKey: 'slideGen.agents.planner', avatar: 'L', role: 'Planner' },
  writer: { color: '#0891b2', nameKey: 'slideGen.agents.writer', avatar: 'W', role: 'Writer' },
  user: { color: '#475569', nameKey: 'slideGen.agents.user', avatar: 'U', role: 'User' },
  status: { color: '#64748b', nameKey: 'slideGen.agents.status', avatar: 'S', role: 'Status' },
  error: { color: '#dc2626', nameKey: 'slideGen.agents.error', avatar: '!', role: 'Error' }
}

const agentAvatarPaths: Record<string, string> = {
  designer: slideGenAgentAvatar('DesignerAgent'),
  reviewer: slideGenAgentAvatar('ReviewerAgent'),
  planner: slideGenAgentAvatar('PlannerAgent')
}

const config = computed(() => {
  if (props.messageType === 'status') return agentConfigs.status
  if (props.messageType === 'error') return agentConfigs.error
  if (props.messageType === 'user_input') return agentConfigs.user
  return agentConfigs[props.agentType] || agentConfigs.status
})

const agentColor = computed(() => config.value.color)
const avatarText = computed(() => config.value.avatar)
const roleLabel = computed(() => config.value.role)

const avatarSrc = computed(() => {
  if (props.messageType === 'user_input') return props.userAvatar || ''
  if (props.messageType === 'status') return slideGenAgentAvatar('System')
  if (props.messageType === 'error') return ''
  if (props.agentType === 'writer') {
    const match = props.agentName?.match(/writer[_\s]*(\d)/i)
    return slideGenAgentAvatar(`WriterAgent${match?.[1] || '1'}`)
  }
  return agentAvatarPaths[props.agentType] || ''
})

const displayName = computed(() => props.agentName || t(config.value.nameKey))

const messageClass = computed(() => ({
  'is-user': props.messageType === 'user_input',
  'is-status': props.messageType === 'status',
  'is-error': props.messageType === 'error'
}))

const formattedTime = computed(() => {
  const date = props.timestamp ? new Date(props.timestamp) : new Date()
  if (Number.isNaN(date.getTime())) return ''
  const lang = String(i18n.global.locale || '').toLowerCase()
  const locale = lang.startsWith('en') ? 'en-US' : lang.includes('malay') || lang.startsWith('ms') ? 'ms-MY' : lang.includes('hindi') || lang.startsWith('hi') ? 'hi-IN' : 'zh-CN'
  return date.toLocaleTimeString(locale, {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

const messageBodyRef = ref<HTMLElement | null>(null)

const renderedContent = computed(() => marked.parse(props.content, { breaks: true, gfm: true }))

watch(renderedContent, () => {
  nextTick(() => renderMermaidIn(messageBodyRef.value))
})
onMounted(() => {
  nextTick(() => renderMermaidIn(messageBodyRef.value))
})
</script>

<style scoped>
.message-wrapper {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 0.75rem;
  margin-bottom: 1rem;
  animation: messageIn 240ms ease-out;
}

.message-wrapper.is-user {
  grid-template-columns: minmax(0, 1fr) 44px;
}

.message-wrapper.is-user .avatar-shell,
.message-wrapper.is-user .avatar-placeholder {
  grid-column: 2;
  grid-row: 1;
}

.message-wrapper.is-user .message-card {
  grid-column: 1;
  grid-row: 1;
  justify-self: end;
  border-color: rgba(114, 46, 209, 0.28);
  background: linear-gradient(135deg, #722ed1 0%, #8b47ea 100%);
  color: #fff;
}

.avatar-shell,
.avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0;
}

.message-card {
  width: fit-content;
  max-width: min(780px, 100%);
  padding: 0.9rem 1rem;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 34px rgba(30, 41, 59, 0.08);
}

.is-status .message-card {
  background: rgba(248, 250, 252, 0.9);
  color: #475569;
}

.is-error .message-card {
  border-color: rgba(220, 38, 38, 0.24);
  background: #fff1f2;
  color: #991b1b;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.55rem;
}

.agent-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.45rem;
}

.agent-name {
  color: #111827;
  font-size: 0.9rem;
  font-weight: 700;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0.14rem 0.5rem;
  border-radius: 999px;
  background: rgba(114, 46, 209, 0.08);
  color: #5b21b6;
  font-size: 0.72rem;
  font-weight: 700;
}

.timestamp {
  color: #8a8fa3;
  font-size: 0.74rem;
  white-space: nowrap;
}

.is-user .agent-name,
.is-user .timestamp,
.is-user .message-body,
.is-user .message-body :deep(p),
.is-user .message-body :deep(li) {
  color: rgba(255, 255, 255, 0.94);
}

.is-user .role-badge {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.message-body {
  color: #273142;
  font-size: 0.95rem;
  line-height: 1.7;
  word-break: break-word;
}

.message-body :deep(h1),
.message-body :deep(h2),
.message-body :deep(h3) {
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  color: inherit;
  line-height: 1.35;
}

.message-body :deep(h1) { font-size: 1.3rem; }
.message-body :deep(h2) { font-size: 1.15rem; }
.message-body :deep(h3) { font-size: 1rem; }

.message-body :deep(ul),
.message-body :deep(ol) {
  margin: 0.5rem 0;
  padding-left: 1.25rem;
}

.message-body :deep(li) {
  margin-bottom: 0.25rem;
}

.message-body :deep(p) {
  margin-bottom: 0.55rem;
  color: #273142;
}

.message-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-body :deep(code) {
  padding: 0.12rem 0.34rem;
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.07);
  font-size: 0.9em;
}

.message-body :deep(pre) {
  margin: 0.65rem 0;
  padding: 0.85rem;
  overflow-x: auto;
  border-radius: 10px;
  background: #111827;
  color: #f8fafc;
}

.message-body :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 720px) {
  .message-wrapper,
  .message-wrapper.is-user {
    grid-template-columns: 36px minmax(0, 1fr);
  }

  .message-wrapper.is-user .avatar-shell,
  .message-wrapper.is-user .avatar-placeholder,
  .message-wrapper.is-user .message-card {
    grid-column: auto;
    grid-row: auto;
  }

  .message-wrapper.is-user {
    direction: rtl;
  }

  .message-wrapper.is-user .message-card {
    direction: ltr;
  }

  .avatar-shell,
  .avatar-placeholder {
    width: 36px;
    height: 36px;
    border-radius: 10px;
  }

  .message-card {
    max-width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .message-wrapper {
    animation: none;
  }
}
</style>
