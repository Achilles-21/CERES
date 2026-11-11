<script lang="ts" setup>
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { query } = useRoute()
const router = useRouter()
const editor = ref<any>(null)
const bntlist = computed(() => [
  { title: 'PPT', path: '/viewppt', color: '#0891b2', icon: 'solar--presentation-graph-bold' },
  { title: proxy.$t('pastlist.notes'), path: '/note', color: '#722ed1', icon: 'solar--notes-bold' },
  { title: proxy.$t('pastlist.report'), path: '/report', color: '#059669', icon: 'solar--document-text-bold' }
])
const isEditing = ref<boolean>(false)
const loading = ref(false)
const valHtml = ref<string>('')
const id = ref(0)
const onChangeState = () => {
  isEditing.value ? onSave() : onEdit()
}
const getNote = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.note.getNote({ videoId: query.id })
    valHtml.value = res.data?.content || ''
    id.value = res.data?.id || 0
  } finally {
    loading.value = false
  }
}
const onSave = async () => {
  const res = await editor.value?.updateNote(id.value)
  if (res?.code === 200) {
    isEditing.value = false
    getNote()
  }
}
const onEdit = () => {
  isEditing.value = true
}
const goResource = (path: string) => {
  router.push(path + '?id=' + query.id)
}
onMounted(() => {
  getNote()
})
</script>

<template>
  <XwyaLayout>
    <main class="note-page ceres-gradient-bg">
      <section class="note-shell">
        <header class="note-header">
          <div>
            <div class="note-eyebrow ceres-pill">
              <span class="iconify solar--notes-bold" aria-hidden="true"></span>
              <span>{{ $t('pastlist.notes') }}</span>
            </div>
            <h1>{{ $t('pastlist.notes') }}</h1>
            <p>{{ $t('pastlist.report') }} / PPT / {{ $t('video.continue') }}</p>
          </div>
          <span class="note-mode ceres-pill">
            <span class="iconify" :class="isEditing ? 'solar--pen-new-square-bold' : 'solar--book-bookmark-bold'" aria-hidden="true"></span>
            <span>{{ isEditing ? $t('pastlist.edit') : $t('video.continue') }}</span>
          </span>
        </header>

        <section class="note-card ceres-card">
          <div class="note-card-header">
            <div>
              <strong>{{ $t('pastlist.notes') }}</strong>
              <span>{{ isEditing ? $t('system.pleaseEnterContent') : $t('pastlist.noNotes') }}</span>
            </div>
          </div>

          <div class="note-content-frame">
            <div v-if="loading" class="note-state" aria-busy="true">
              <span class="iconify eos-icons--three-dots-loading" aria-hidden="true"></span>
              <p>Loading note</p>
            </div>
            <n-scrollbar v-else-if="!isEditing" class="note-reader-scroll">
              <article v-if="valHtml" class="note-reader" v-html="valHtml"></article>
              <div v-else class="note-state">
                <span class="iconify solar--document-add-bold" aria-hidden="true"></span>
                <p>{{ $t('pastlist.noNotes') }}</p>
              </div>
            </n-scrollbar>
            <div v-else class="note-editor-frame">
              <XwyaEditor :videoId="query.id" ref="editor" :v="valHtml"></XwyaEditor>
            </div>
          </div>
        </section>

        <div class="note-toolbar-zone">
          <nav class="note-toolbar ceres-glass" aria-label="Note actions">
            <button
              v-for="(p, i) in bntlist"
              :key="i"
              class="note-action ceres-pill"
              type="button"
              :style="{ '--action-color': p.color }"
              @click="goResource(p.path)"
            >
              <span class="iconify" :class="p.icon" aria-hidden="true"></span>
              <span>{{ p.title }}</span>
            </button>
            <button class="note-action note-action-primary ceres-pill" type="button" @click="onChangeState">
              <span class="iconify" :class="isEditing ? 'solar--diskette-bold' : 'solar--pen-new-square-bold'" aria-hidden="true"></span>
              <span>{{ isEditing ? $t('pastlist.save') : $t('pastlist.edit') }}</span>
            </button>
          </nav>
        </div>
      </section>
    </main>
  </XwyaLayout>
</template>

<style lang="css" scoped>
.note-page {
  width: 100%;
  height: 100%;
  overflow: hidden;
  color: #0f172a;
}

.note-shell {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  gap: 14px;
  padding: clamp(14px, 2vw, 24px);
}

.note-header {
  display: flex;
  flex-shrink: 0;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.note-eyebrow,
.note-mode {
  border-color: rgba(114, 46, 209, 0.2);
  background: rgba(255, 255, 255, 0.82);
  color: #5b21b6;
}

.note-header h1 {
  margin: 12px 0 6px;
  color: #111827;
  font-size: clamp(26px, 3.4vw, 40px);
  font-weight: 850;
  line-height: 1.15;
}

.note-header p {
  margin: 0;
  color: #475569;
  font-size: 15px;
}

.note-card {
  display: flex;
  min-height: 0;
  flex: 1;
  overflow: hidden;
  flex-direction: column;
  border-color: rgba(114, 46, 209, 0.14);
  background: rgba(255, 255, 255, 0.9);
}

.note-card-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(114, 46, 209, 0.1);
  padding: 14px 18px;
}

.note-card-header > div {
  display: grid;
  gap: 3px;
}

.note-card-header strong {
  color: #111827;
  font-size: 16px;
}

.note-card-header span {
  color: #64748b;
  font-size: 13px;
}

.note-content-frame {
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

.note-reader-scroll,
.note-editor-frame {
  width: 100%;
  height: 100%;
}

.note-reader {
  max-width: 820px;
  margin: 0 auto;
  padding: clamp(22px, 3vw, 42px);
  color: #1f2937;
  font-size: 16px;
  line-height: 1.78;
}

.note-reader :deep(img) {
  max-width: 100%;
  border-radius: 12px;
}

.note-reader :deep(p),
.note-reader :deep(ul),
.note-reader :deep(ol) {
  margin: 0 0 14px;
}

.note-state {
  display: grid;
  height: 100%;
  min-height: 220px;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #64748b;
  text-align: center;
}

.note-state .iconify {
  color: #722ed1;
  font-size: 38px;
}

.note-toolbar-zone {
  display: flex;
  flex-shrink: 0;
  justify-content: center;
  padding: 2px 0 4px;
}

.note-toolbar {
  display: flex;
  max-width: 100%;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  border-radius: 999px;
  padding: 10px;
}

.note-action {
  min-width: 138px;
  min-height: 42px;
  border-color: color-mix(in srgb, var(--action-color, #722ed1) 22%, transparent);
  color: var(--action-color, #722ed1);
  cursor: pointer;
  padding: 0 18px;
  transition: border-color 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.note-action:hover,
.note-action:focus-visible {
  border-color: color-mix(in srgb, var(--action-color, #722ed1) 46%, transparent);
  background: color-mix(in srgb, var(--action-color, #722ed1) 12%, white);
  box-shadow: 0 10px 26px color-mix(in srgb, var(--action-color, #722ed1) 18%, transparent);
  outline: none;
}

.note-action-primary {
  --action-color: #722ed1;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
}

.note-action-primary:hover,
.note-action-primary:focus-visible {
  color: #fff;
}

@media (max-width: 720px) {
  .note-shell {
    padding: 10px;
  }

  .note-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .note-mode {
    align-self: flex-start;
  }

  .note-toolbar {
    width: 100%;
    border-radius: 24px;
  }

  .note-action {
    min-width: min(100%, 132px);
    flex: 1 1 132px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .note-action {
    transition: none;
  }
}
</style>
