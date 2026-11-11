<script lang="ts" setup>
import { marked } from 'marked'
import '@/utils/mermaid'
import { renderMermaidIn } from '@/utils/mermaid'
import i18n from '@/lang'

const { query } = useRoute()
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const info = ref<any>({})
const loading = ref(false)
const contentHtml = computed(() => marked.parse(info.value?.content || '') as string)
const reportProseRef = ref<HTMLElement | null>(null)
const updatedAt = computed(() => info.value?.updateTime || info.value?.updatedTime || info.value?.createTime || 'No update time')
const plainContent = computed(() => (info.value?.content || '').replace(/[#>*_`[\]()!-]/g, ' ').replace(/\s+/g, ' ').trim())
const reportSummary = computed(() => {
  if (!plainContent.value) return i18n.global.t('report.noSummary')
  return plainContent.value.length > 160 ? plainContent.value.slice(0, 160) + '...' : plainContent.value
})
const getReport = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.system.getReport({ videoId: query.id })
    info.value = res.data || {}
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  getReport()
})

watch(contentHtml, () => {
  nextTick(() => renderMermaidIn(reportProseRef.value))
})
</script>

<template>
  <XwyaLayout>
    <main class="report-page ceres-gradient-bg">
      <section class="report-shell ceres-page-shell">
        <article class="report-document ceres-card">
          <header class="report-header">
            <div class="report-eyebrow ceres-pill">
              <span class="iconify solar--document-text-bold" aria-hidden="true"></span>
              <span>{{ $t('pastlist.report') }}</span>
            </div>
            <h1>{{ info.title || $t('pastlist.report') }}</h1>
            <div class="report-meta">
              <span class="ceres-pill">
                <span class="iconify solar--clock-circle-bold" aria-hidden="true"></span>
                <span>{{ $t('report.updatedAt', { time: updatedAt }) }}</span>
              </span>
              <span class="ceres-pill">
                <span class="iconify solar--stethoscope-bold" aria-hidden="true"></span>
                <span>{{ $t('report.medicalTeachingReport') }}</span>
              </span>
            </div>
            <section class="report-summary" aria-label="Report summary">
              <span class="summary-icon iconify solar--clipboard-text-bold" aria-hidden="true"></span>
              <div>
                <strong>{{ $t('report.summary') }}</strong>
                <p>{{ reportSummary }}</p>
              </div>
            </section>
          </header>

          <div v-if="loading" class="report-state" aria-busy="true">
            <span class="iconify eos-icons--three-dots-loading" aria-hidden="true"></span>
            <p>Loading report</p>
          </div>
          <div v-else-if="info.content" ref="reportProseRef" class="report-prose" v-html="contentHtml"></div>
          <div v-else class="report-state">
            <span class="iconify solar--document-add-bold" aria-hidden="true"></span>
            <p>{{ $t('system.nodata') }}</p>
          </div>
        </article>
      </section>
    </main>
  </XwyaLayout>
</template>

<style lang="scss" scoped>
.report-page {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  color: #0f172a;
}

.report-shell {
  max-width: 980px;
  padding: clamp(16px, 3vw, 34px);
}

.report-document {
  width: min(100%, 860px);
  margin: 0 auto;
  overflow: hidden;
  border-color: rgba(114, 46, 209, 0.14);
  background: rgba(255, 255, 255, 0.94);
}

.report-header {
  padding: clamp(24px, 4vw, 42px) clamp(22px, 4vw, 46px) 24px;
  border-bottom: 1px solid rgba(114, 46, 209, 0.12);
  background:
    radial-gradient(circle at top left, rgba(114, 46, 209, 0.14), transparent 34%),
    linear-gradient(135deg, rgba(139, 71, 234, 0.08), rgba(8, 145, 178, 0.06));
}

.report-eyebrow {
  width: max-content;
  border-color: rgba(114, 46, 209, 0.22);
  background: rgba(255, 255, 255, 0.82);
  color: #5b21b6;
}

.report-header h1 {
  margin: 16px 0 14px;
  color: #111827;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 850;
  line-height: 1.16;
}

.report-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.report-meta .ceres-pill {
  min-height: 34px;
  border-color: rgba(114, 46, 209, 0.16);
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  padding: 0 12px;
}

.report-summary {
  display: flex;
  gap: 14px;
  border: 1px solid rgba(8, 145, 178, 0.16);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  padding: 16px;
}

.summary-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: rgba(8, 145, 178, 0.12);
  color: #0891b2;
  font-size: 22px;
}

.report-summary strong {
  color: #111827;
  font-size: 15px;
}

.report-summary p {
  margin: 6px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.report-prose {
  max-width: 820px;
  margin: 0 auto;
  padding: clamp(24px, 4vw, 46px);
  color: #1f2937;
  font-size: 16px;
  line-height: 1.82;
}

.report-prose :deep(h1),
.report-prose :deep(h2),
.report-prose :deep(h3) {
  color: #111827;
  font-weight: 850;
  line-height: 1.25;
}

.report-prose :deep(h1) {
  margin: 0 0 20px;
  font-size: 30px;
}

.report-prose :deep(h2) {
  margin: 30px 0 12px;
  padding-top: 8px;
  font-size: 24px;
}

.report-prose :deep(h3) {
  margin: 24px 0 10px;
  font-size: 19px;
}

.report-prose :deep(p),
.report-prose :deep(ul),
.report-prose :deep(ol),
.report-prose :deep(blockquote),
.report-prose :deep(table) {
  margin: 0 0 16px;
}

.report-prose :deep(ul),
.report-prose :deep(ol) {
  padding-left: 24px;
}

.report-prose :deep(li) {
  margin-bottom: 8px;
}

.report-prose :deep(blockquote) {
  border-left: 4px solid #8b47ea;
  border-radius: 0 12px 12px 0;
  background: rgba(114, 46, 209, 0.07);
  color: #334155;
  padding: 14px 18px;
}

.report-prose :deep(code) {
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.08);
  color: #5b21b6;
  padding: 2px 6px;
}

.report-prose :deep(pre) {
  overflow-x: auto;
  border-radius: 14px;
  background: #111827;
  color: #f8fafc;
  padding: 16px;
}

.report-prose :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}

.report-prose :deep(table) {
  display: block;
  width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
}

.report-prose :deep(th),
.report-prose :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 10px 12px;
  text-align: left;
}

.report-prose :deep(th) {
  background: rgba(114, 46, 209, 0.08);
}

.report-state {
  display: grid;
  min-height: 320px;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #64748b;
  text-align: center;
}

.report-state .iconify {
  color: #722ed1;
  font-size: 42px;
}

@media (max-width: 640px) {
  .report-shell {
    padding: 10px;
  }

  .report-header,
  .report-prose {
    padding-inline: 18px;
  }

  .report-summary {
    flex-direction: column;
  }
}
</style>
