<template>
  <form class="slidegen-input ceres-card" @submit.prevent="send">
    <textarea
      ref="inputRef"
      v-model="inputText"
      :placeholder="placeholder"
      rows="3"
      :disabled="disabled"
      :aria-label="$t('slideGen.inputAria')"
      @keydown="handleKeydown"
    ></textarea>

    <div class="input-tools">
      <div class="tool-group" aria-hidden="true">
        <span class="shortcut-pill">Ctrl / Cmd + Enter</span>
      </div>

      <button
        type="submit"
        class="send-button ceres-icon-button"
        :disabled="disabled || !inputText.trim()"
        :aria-label="$t('slideGen.sendDemand')"
        :title="$t('slideGen.sendDemand')"
      >
        <span class="iconify solar--arrow-up-linear"></span>
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import i18n from '@/lang'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: () => i18n.global.t('slideGen.inputDefaultPlaceholder')
  }
})

const emit = defineEmits<{ send: [content: string] }>()

const inputText = ref('')
const inputRef = ref<HTMLTextAreaElement | null>(null)

const send = () => {
  const content = inputText.value.trim()
  if (!content || props.disabled) return
  emit('send', content)
  inputText.value = ''
}

const handleKeydown = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    send()
  }
}
</script>

<style scoped>
.slidegen-input {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding: 0.9rem;
  border: 1px solid rgba(114, 46, 209, 0.14);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(55, 48, 163, 0.11);
}

textarea {
  width: 100%;
  min-height: 92px;
  padding: 0.9rem 1rem;
  border: 1px solid rgba(99, 102, 241, 0.18);
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfbff 100%);
  color: #111827;
  font-size: 0.98rem;
  font-family: inherit;
  line-height: 1.6;
  resize: vertical;
  transition: border-color 180ms ease, box-shadow 180ms ease, background-color 180ms ease;
}

textarea::placeholder {
  color: #8a8fa3;
}

textarea:hover:not(:disabled) {
  border-color: rgba(114, 46, 209, 0.35);
}

textarea:focus-visible {
  outline: none;
  border-color: #722ed1;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.14);
}

textarea:disabled {
  background: #f4f4f7;
  color: #8a8fa3;
  cursor: not-allowed;
}

.input-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.tool-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.78rem;
}

.shortcut-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  min-height: 28px;
  padding: 0.28rem 0.62rem;
  border-radius: 999px;
  border: 1px solid rgba(114, 46, 209, 0.14);
  background: rgba(15, 23, 42, 0.04);
  color: #697386;
  border-color: rgba(15, 23, 42, 0.08);
  white-space: nowrap;
}

.send-button {
  width: 44px;
  height: 44px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #722ed1 0%, #8b47ea 100%);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 14px 28px rgba(114, 46, 209, 0.28);
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease, background 180ms ease;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 34px rgba(114, 46, 209, 0.34);
}

.send-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18), 0 18px 34px rgba(114, 46, 209, 0.34);
}

.send-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  box-shadow: none;
}

.send-button .iconify {
  width: 20px;
  height: 20px;
}

@media (max-width: 640px) {
  .input-tools {
    align-items: flex-end;
  }

  .shortcut-pill {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .slidegen-input,
  .slidegen-input * {
    transition: none !important;
  }
}
</style>
