<script lang="ts" setup>
import i18n from '@/lang'
import useSystemStore from '@/store/useSystemStore'

const props = defineProps({
  onEnter: {
    type: Function,
    default: () => { }
  },
  isEnter: {
    type: Boolean,
    default: false
  }
})
const { query, path } = useRoute()
const systemStore = useSystemStore()
const editorPlaceholder = computed(() => {
  systemStore.lang
  return i18n.global.t('chat.inputPlaceholder')
})

const { proxy } = getCurrentInstance()
const regex = /^image\/(jpeg|jpg|png|gif|bmp|webp|svg)$/
const contentRef = ref<any>(null)
const fileInputRef = ref<any>(null)
const base64Arr = ref()
const pendingImageMinioUrl = ref('')
const attachmentName = computed(() => {
  if (!pendingImageMinioUrl.value) return ''
  const name = pendingImageMinioUrl.value.split('/').filter(Boolean).pop()
  return name || 'image'
})
const pamas = ref<any>({
  content: '',
  answer: '',
  queId: 0
})

const processImageFile = async (file: File) => {
  const reader = new FileReader()
  reader.readAsDataURL(file)
  await new Promise<void>(resolve => {
    reader.onload = () => {
      base64Arr.value = (reader.result as string).split(',')[1]
      resolve()
    }
  })
  const formData = new FormData()
  formData.append('folderName', 'chatImage')
  formData.append('file', file)
  const res = await fetch(import.meta.env.VITE_APP_URL + '/file/upload', {
    method: 'POST',
    headers: { 'Authorization': localStorage.getItem('token') },
    body: formData,
  })
  const json = await res.json()
  if (json.code === 200) {
    pendingImageMinioUrl.value = json.data.fileUrl
  }
}

const triggerImageUpload = () => fileInputRef.value?.click()

const clearPendingImage = () => {
  pendingImageMinioUrl.value = ''
  base64Arr.value = ''
}

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file && regex.test(file.type)) processImageFile(file)
  ;(e.target as HTMLInputElement).value = ''
}

const handlePaste = async (e) => {
  e.stopPropagation()
  e.preventDefault()

  if (e.clipboardData.files.length > 0) {
    if (regex.test(e.clipboardData.files[0].type)) {
      await processImageFile(e.clipboardData.files[0])
    }
  }
  let text = '',
    event = (e.originalEvent || e)
  if (event.clipboardData && event.clipboardData.getData) {
    text = event.clipboardData.getData('text/plain')
  } else if ((window as any).clipboardData && (window as any).clipboardData.getData) {
    text = (window as any).clipboardData.getData('text')
  }
  if (document.queryCommandSupported('insertText')) {
    document.execCommand('insertText', false, text)
  } else {
    document.execCommand('paste', false, text)
  }
}
const setCaretToEnd = (element) => {
  const range = document.createRange()
  const selection = window.getSelection()
  range.selectNodeContents(element)
  range.collapse(false)
  selection.removeAllRanges()
  selection.addRange(range)
}
const submitContent = () => {
  if (!props.isEnter) {
    let isQue = pamas.value.queId ? 1 : 0
    if (path === '/exam') {
      isQue = 2
    }
    let content = contentRef.value.innerHTML
    if (pendingImageMinioUrl.value) {
      content += `<img src="${pendingImageMinioUrl.value}" class="xwya-message-image">`
    }
    props.onEnter(content, toRaw(base64Arr.value), query.id, pamas.value.content, pamas.value.answer, isQue, pamas.value.queId, isQue)
    if (pamas.value.close) {
      pamas.value.close()
    }

    pamas.value = {
      content: '',
      answer: '',
      queId: 0
    }
    clearPendingImage()
    contentRef.value.innerHTML = ''
  } else {
    window.$msg.info(i18n.global.t('chat.endrecord'))
  }
}
const handleKeydown = (e) => {
  if (e.ctrlKey && e.keyCode === 13) {
    contentRef.value.appendChild(document.createElement('br'))
    setCaretToEnd(contentRef.value)
    return
  }
  if (e.keyCode === 13) {
    e.preventDefault()
    submitContent()
  }
}
const addContent = (content) => {
  contentRef.value.innerHTML = contentRef.value.innerHTML + content
  setCaretToEnd(contentRef.value)
}
const setPamas = (obj) => {
  pamas.value = obj
}

defineExpose({
  addContent,
  triggerImageUpload,
  submitContent,
  clearPendingImage,
  pendingImageMinioUrl
})
onMounted(() => {
  proxy.$mitt.on('setPamas', setPamas)
})

onBeforeUnmount(() => {
  proxy.$mitt.off('setPamas', setPamas)
})
</script>

<template>
  <div class="editor-content-shell">
    <input ref="fileInputRef" type="file" accept="image/*" class="sr-only" @change="handleFileChange" />
    <div
      ref="contentRef"
      role="textbox"
      aria-multiline="true"
      tabindex="0"
      @paste="handlePaste"
      @keydown="handleKeydown"
      contenteditable
      class="input-text-content"
      :data-placeholder="editorPlaceholder"
      :aria-label="editorPlaceholder"
    ></div>
    <div v-if="pendingImageMinioUrl" class="attachment-chip" :title="attachmentName">
      <span class="iconify solar--gallery-bold attachment-icon"></span>
      <span class="attachment-name">{{ attachmentName }}</span>
      <button type="button" class="attachment-clear" aria-label="Remove attachment" @click="clearPendingImage">
        <span class="iconify solar--close-circle-bold"></span>
      </button>
    </div>
  </div>
</template>

<style lang="css" scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.editor-content-shell {
  position: relative;
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}

.input-text-content {
  position: relative;
  flex: 1;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  border: 0;
  line-height: 1.5;
  min-height: 124px;
  max-height: 180px;
  padding: 0;
  color: inherit;
  font-size: 15px;
  word-break: break-word;
  white-space: pre-wrap;
  overflow-y: auto;
  overflow-x: hidden;
}

.a {
  display: inline-block;
}

.input-text-content:empty::before {
  content: attr(data-placeholder);
  color: var(--prompt-editor-placeholder, var(--ceres-muted));
  pointer-events: none;
}

.input-text-content:focus {
  outline: none !important;
}

.input-text-content :deep(p) {
  margin: 0 0 8px;
}

.input-text-content :deep(ul),
.input-text-content :deep(ol) {
  margin: 8px 0 8px 20px;
  padding-left: 12px;
}

.input-text-content :deep(li + li) {
  margin-top: 4px;
}

.input-text-content :deep(code) {
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 6px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  padding: 1px 5px;
  font-size: 0.92em;
}

.input-text-content :deep(pre) {
  margin: 10px 0;
  overflow-x: auto;
  border: 1px solid var(--ceres-border);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.94);
  color: #f8fafc;
  padding: 12px;
}

.input-text-content :deep(pre code) {
  border: 0;
  background: transparent;
  color: inherit;
  padding: 0;
}

.input-text-content :deep(img) {
  display: block;
  max-width: min(260px, 100%);
  height: auto;
  margin-top: 8px;
  border: 1px solid var(--ceres-border);
  border-radius: 10px;
}

.attachment-chip {
  display: inline-flex;
  max-width: 100%;
  width: fit-content;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.22);
  border-radius: 999px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  padding: 5px 8px 5px 10px;
  font-size: 12px;
  font-weight: 700;
}

.attachment-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.attachment-name {
  min-width: 0;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-clear {
  display: inline-flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: currentColor;
  cursor: pointer;
  transition: background-color 180ms ease, color 180ms ease;
}

.attachment-clear:hover,
.attachment-clear:focus-visible {
  background: rgba(var(--ceres-primary-rgb), 0.14);
  outline: none;
}

.input-text-content::-webkit-scrollbar {
  width: 4px;
}

.input-text-content::-webkit-scrollbar-track {
  background-color: transparent;
}

.input-text-content::-webkit-scrollbar-thumb {
  background-color: rgba(var(--ceres-primary-rgb), 0.3);
  border-radius: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .attachment-clear {
    transition: none;
  }
}
</style>
