<template>
  <div class="user-input">
    <div class="input-container">
      <textarea
        ref="inputRef"
        v-model="inputText"
        :placeholder="placeholder"
        rows="3"
        :disabled="disabled"
        @keydown="handleKeydown"
      ></textarea>
      <button
        class="send-button"
        :disabled="disabled || !inputText.trim()"
        @click="send"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
          <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
        </svg>
      </button>
    </div>
    <p class="hint">按 Ctrl+Enter 或 Cmd+Enter 发送</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入您的课件需求...'
  }
})

const emit = defineEmits(['send'])

const inputText = ref('')
const inputRef = ref(null)

const send = () => {
  if (!inputText.value.trim() || props.disabled) return
  emit('send', inputText.value)
  inputText.value = ''
}

const handleKeydown = (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    send()
  }
}
</script>

<style scoped>
.user-input {
  background: white;
  border-top: 1px solid #e0e0e0;
  padding: 1rem 1.5rem;
}

.input-container {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

textarea {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s;
  line-height: 1.5;
}

textarea:focus {
  outline: none;
  border-color: #667eea;
}

textarea:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-button {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-button svg {
  width: 20px;
  height: 20px;
}

.hint {
  font-size: 0.75rem;
  color: #999;
  margin-top: 0.5rem;
  text-align: right;
}
</style>
