<script lang="ts" setup>
const { query } = useRoute()
const router = useRouter()
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const pptRef = ref<HTMLElement | null>(null)
const url = ref('')
const loading = ref(false)
const frameLoading = ref(false)
const iframeKey = ref(0)
const getPPT = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.system.getPPT({ videoId: query.id })
    url.value = res.data || ''
    iframeKey.value += 1
    frameLoading.value = Boolean(url.value)
  } finally {
    loading.value = false
    if (!url.value) frameLoading.value = false
  }
}
const refreshPreview = () => {
  getPPT()
}
const downloadPPT = () => {
  if (!url.value) return
  const link = document.createElement('a')
  link.href = url.value
  link.download = 'course-presentation'
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  link.click()
}
const toggleFullscreen = async () => {
  const target = pptRef.value
  if (!target) return
  if (document.fullscreenElement) {
    await document.exitFullscreen()
    return
  }
  await target.requestFullscreen()
}
onMounted(() => {
  getPPT()
})
</script>

<template>
  <main class="ppt-page ceres-gradient-bg">
    <header class="ppt-toolbar ceres-glass">
      <button class="ppt-back ceres-pill" type="button" @click="router.back()">
        <span class="iconify solar--arrow-left-linear" aria-hidden="true"></span>
        <span>{{ $t('pptPreview.back') }}</span>
      </button>

      <div class="ppt-title">
        <span class="ppt-title-icon iconify solar--presentation-graph-bold" aria-hidden="true"></span>
        <div>
          <strong>PPT</strong>
          <small>{{ $t('pptPreview.subtitle') }}</small>
        </div>
      </div>

      <div class="ppt-actions" aria-label="PPT actions">
        <button class="ceres-icon-button ppt-action" type="button" :title="$t('pptPreview.download')" :aria-label="$t('pptPreview.download')" :disabled="!url" @click="downloadPPT">
          <span class="iconify solar--download-minimalistic-bold" aria-hidden="true"></span>
        </button>
        <button class="ceres-icon-button ppt-action" type="button" :title="$t('pptPreview.fullscreen')" :aria-label="$t('pptPreview.fullscreen')" :disabled="!url" @click="toggleFullscreen">
          <span class="iconify solar--full-screen-bold" aria-hidden="true"></span>
        </button>
        <button class="ceres-icon-button ppt-action" type="button" :title="$t('pptPreview.refresh')" :aria-label="$t('pptPreview.refresh')" @click="refreshPreview">
          <span class="iconify solar--restart-bold" aria-hidden="true"></span>
        </button>
      </div>
    </header>

    <section class="ppt-stage ceres-card" ref="pptRef">
      <iframe
        v-if="url"
        :key="iframeKey"
        :src="url"
        title="PPT preview"
        width="100%"
        height="100%"
        frameborder="0"
        allowfullscreen
        @load="frameLoading = false"
      ></iframe>
      <div v-if="loading || frameLoading" class="ppt-state ppt-state-overlay" aria-busy="true">
        <span class="iconify eos-icons--three-dots-loading" aria-hidden="true"></span>
        <p>Loading PPT</p>
      </div>
      <div v-else-if="!url" class="ppt-state">
        <span class="iconify solar--presentation-graph-bold" aria-hidden="true"></span>
        <p>{{ $t('system.nodata') }}</p>
      </div>
    </section>
  </main>
</template>

<style>
.pptx-preview-wrapper {
  background: transparent !important;
}
</style>

<style lang="scss" scoped>
.ppt-page {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  flex-direction: column;
  gap: 14px;
  padding: clamp(10px, 1.8vw, 18px);
  color: #0f172a;
}

.ppt-toolbar {
  display: flex;
  min-height: 64px;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-radius: 20px;
  padding: 10px 12px;
}

.ppt-back {
  min-width: 96px;
  cursor: pointer;
  padding: 0 16px;
}

.ppt-title {
  display: inline-flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #111827;
}

.ppt-title-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #722ed1, #8b47ea 60%, #0891b2);
  color: #fff;
  font-size: 22px;
}

.ppt-title strong,
.ppt-title small {
  display: block;
}

.ppt-title strong {
  font-size: 17px;
  font-weight: 850;
  line-height: 1.2;
}

.ppt-title small {
  color: #64748b;
  font-size: 12px;
  line-height: 1.3;
}

.ppt-actions {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}

.ppt-action {
  cursor: pointer;
}

.ppt-action:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.ppt-back:focus-visible,
.ppt-action:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.22);
  outline-offset: 3px;
}

.ppt-stage {
  position: relative;
  min-height: 0;
  flex: 1;
  overflow: hidden;
  border-color: rgba(114, 46, 209, 0.14);
  border-radius: 22px;
  background: #0f172a;
}

.ppt-stage iframe {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.ppt-state {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.92), rgba(49, 46, 129, 0.88));
  color: #e2e8f0;
  text-align: center;
}

.ppt-state-overlay {
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(6px);
}

.ppt-state .iconify {
  color: #c4b5fd;
  font-size: 44px;
}

.ppt-state p {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
}

@media (max-width: 720px) {
  .ppt-toolbar {
    align-items: stretch;
    flex-wrap: wrap;
  }

  .ppt-title {
    order: 3;
    flex-basis: 100%;
    justify-content: flex-start;
  }

  .ppt-back {
    min-width: 0;
  }

  .ppt-actions {
    margin-left: auto;
  }
}
</style>
