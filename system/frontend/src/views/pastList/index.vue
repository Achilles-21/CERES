<script lang="ts" setup>
import usePage from '@/hooks/usePage'

const { page, total, loading, data } = usePage(8)
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { push } = useRouter()

const actionList = computed(() => [
  {
    title: proxy.$t('pastlist.ppt'),
    path: '/viewppt',
    color: '#0891b2',
    icon: 'iconify ic--outline-featured-video',
    description: proxy.$t('pastlist.ppt')
  },
  {
    title: proxy.$t('pastlist.notes'),
    path: '/note',
    color: '#722ed1',
    icon: 'iconify ic--outline-library-books',
    description: proxy.$t('pastlist.notes')
  },
  {
    title: proxy.$t('pastlist.report'),
    path: '/report',
    color: '#059669',
    icon: 'iconify ic--outline-assignment',
    description: proxy.$t('pastlist.report')
  }
])

const pageConfig = computed(() => ({
  itemCount: total.value,
  pageSizes: [8, 12, 20],
  pageSlot: 7,
  pageSize: page.pageSize,
  page: page.pageNum,
  'onUpdate:page': (p: number) => {
    page.pageNum = p
    getCourseList()
  },
  'onUpdate:pageSize': (p: number) => {
    page.pageSize = p
    page.pageNum = 1
    getCourseList()
  }
}))

const getCourseList = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.video.getCoursePlayList(page)
    total.value = res.data?.total || 0
    data.value = res.data?.row || []
  } finally {
    loading.value = false
  }
}

const getVideoId = (info: any) => info?.videoId || info?.id

const toPage = (p: any, info: any) => {
  if (!info.noteId) {
    window.$msg.error(proxy.$t('pastlist.noNotes'))
    return
  }
  push(p.path + '?id=' + getVideoId(info))
}

const continueStudy = (info: any) => {
  const id = getVideoId(info)
  if (id) {
    push('/study?id=' + id)
  }
}

onMounted(() => {
  getCourseList()
})
</script>

<template>
  <XwyaLayout hide-chat-header>
    <main class="past-page ceres-gradient-bg">
      <section class="past-shell ceres-page-shell">
        <header class="past-header">
          <div>
            <div class="past-eyebrow ceres-pill">
              <span class="iconify solar--archive-bold" aria-hidden="true"></span>
              <span>{{ $t('home.PreviousNotesAndReports') }}</span>
            </div>
            <p>{{ $t('pastlist.notes') }} / {{ $t('pastlist.report') }} / {{ $t('pastlist.ppt') }}</p>
          </div>
          <div class="past-count ceres-glass">
            <span class="iconify solar--checklist-minimalistic-bold" aria-hidden="true"></span>
            <span>{{ total || 0 }}</span>
          </div>
        </header>

        <div class="action-summary" aria-label="Learning resources">
          <div v-for="item in actionList" :key="item.path" class="summary-card ceres-card" :style="{ '--accent-color': item.color }">
            <span class="summary-icon" aria-hidden="true"><span :class="item.icon"></span></span>
            <span>{{ item.description }}</span>
          </div>
        </div>

        <div v-if="loading" class="history-list" aria-busy="true">
          <div v-for="item in 5" :key="item" class="history-card history-skeleton ceres-card">
            <div class="skeleton-main">
              <span></span>
              <span></span>
            </div>
            <div class="skeleton-actions">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <div v-else-if="data.length" class="history-list">
          <article v-for="(info, index) in data" :key="info.id || info.videoId || index" class="history-card ceres-card">
            <div class="history-main">
              <span class="history-index">{{ index + 1 }}</span>
              <div class="history-copy">
                <h2>{{ info.name }}</h2>
              </div>
            </div>

            <div class="history-actions" aria-label="Resource actions">
              <button
                v-for="item in actionList"
                :key="item.path"
                class="history-action ceres-pill"
                type="button"
                :style="{ '--accent-color': item.color }"
                :aria-label="item.title + ' ' + info.name"
                @click="toPage(item, info)"
              >
                <span class="action-icon" aria-hidden="true"><span :class="item.icon"></span></span>
                <span>{{ item.title }}</span>
              </button>
              <button class="continue-action ceres-icon-button" type="button" :aria-label="$t('video.continue')" :title="$t('video.continue')" @click="continueStudy(info)">
                <span class="iconify solar--arrow-right-linear" aria-hidden="true"></span>
              </button>
            </div>
          </article>
        </div>

        <div v-else class="empty-card ceres-card">
          <span class="iconify solar--folder-open-bold" aria-hidden="true"></span>
          <h2>{{ $t('system.nodata') }}</h2>
          <p>{{ $t('pastlist.noNotes') }}</p>
          <button class="empty-action ceres-pill" type="button" @click="push('/course')">
            <span class="iconify solar--play-circle-bold" aria-hidden="true"></span>
            <span>{{ $t('home.goToClass') }}</span>
          </button>
        </div>

        <div v-if="total > page.pageSize" class="past-pagination">
          <n-pagination v-bind="pageConfig" />
        </div>
      </section>
    </main>
  </XwyaLayout>
</template>

<style lang="css" scoped>
.past-page {
  min-height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  color: #0f172a;
}

.past-shell {
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: clamp(18px, 3vw, 38px);
}

.past-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
  animation: fade-up 240ms ease both;
}

.past-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(114, 46, 209, 0.22);
  background: rgba(255, 255, 255, 0.78);
  color: #5b21b6;
  font-weight: 800;
  padding: 8px 14px;
}


.past-header p {
  margin: 0;
  color: #475569;
  font-size: 16px;
}

.past-count {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  padding: 10px 16px;
  color: #4c1d95;
  font-size: 18px;
  font-weight: 850;
}

.action-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
  animation: fade-up 260ms ease both;
}

.summary-card {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
  border: 1px solid color-mix(in srgb, var(--accent-color) 24%, transparent);
  padding: 14px 16px;
  color: #1f2937;
  font-weight: 800;
}

.summary-icon {
  display: inline-flex;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: color-mix(in srgb, var(--accent-color) 12%, white);
  color: var(--accent-color);
  font-size: 22px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  animation: fade-up 280ms ease both;
}

.history-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 520px);
  gap: 18px;
  align-items: center;
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 18px;
  transition: border-color 180ms ease, box-shadow 200ms ease, transform 200ms ease;
}

.history-card:hover {
  border-color: rgba(114, 46, 209, 0.4);
  box-shadow: 0 18px 40px rgba(30, 41, 59, 0.12);
  transform: translateY(-1px);
}

.history-main {
  display: grid;
  min-width: 0;
  grid-template-columns: 46px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.history-index {
  display: inline-flex;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.14), rgba(8, 145, 178, 0.12));
  color: #5b21b6;
  font-weight: 850;
}

.history-copy {
  min-width: 0;
}

.history-copy h2 {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 850;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}


.history-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) 44px;
  gap: 10px;
  align-items: center;
}

.history-action,
.empty-action {
  display: inline-flex;
  min-height: 44px;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid color-mix(in srgb, var(--accent-color, #722ed1) 24%, transparent);
  background: color-mix(in srgb, var(--accent-color, #722ed1) 8%, white);
  color: color-mix(in srgb, var(--accent-color, #722ed1) 86%, #111827);
  font-weight: 850;
  transition: border-color 180ms ease, background 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.history-action:hover,
.empty-action:hover {
  border-color: color-mix(in srgb, var(--accent-color, #722ed1) 50%, transparent);
  background: color-mix(in srgb, var(--accent-color, #722ed1) 13%, white);
  box-shadow: 0 12px 26px rgba(79, 70, 229, 0.13);
  transform: translateY(-1px);
}

.action-icon {
  display: inline-flex;
  font-size: 20px;
}

.continue-action {
  display: inline-flex;
  width: 44px;
  height: 44px;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  font-size: 21px;
  transition: filter 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.continue-action:hover {
  filter: brightness(1.04);
  box-shadow: 0 14px 30px rgba(114, 46, 209, 0.26);
  transform: translateY(-1px);
}

.history-action:focus-visible,
.continue-action:focus-visible,
.empty-action:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.32);
  outline-offset: 3px;
}

.empty-card {
  display: flex;
  min-height: 280px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 34px;
  color: #64748b;
  text-align: center;
}

.empty-card > .iconify {
  color: #722ed1;
  font-size: 46px;
}

.empty-card h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 850;
}

.empty-card p {
  margin: 0 0 8px;
}

.past-pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0 6px;
}

.past-pagination :deep(.n-pagination) {
  flex-wrap: wrap;
  justify-content: center;
}

.history-skeleton {
  min-height: 104px;
}

.skeleton-main,
.skeleton-actions {
  display: flex;
  gap: 10px;
}

.skeleton-main {
  flex-direction: column;
}

.skeleton-actions {
  justify-content: flex-end;
}

.skeleton-main span,
.skeleton-actions span {
  display: block;
  height: 16px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(226, 232, 240, 0.75), rgba(241, 245, 249, 0.95), rgba(226, 232, 240, 0.75));
  background-size: 220% 100%;
  animation: shimmer 1100ms ease-in-out infinite;
}

.skeleton-main span:first-child {
  width: 72%;
}

.skeleton-main span:last-child {
  width: 42%;
}

.skeleton-actions span {
  width: 120px;
  height: 40px;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  to {
    background-position: -220% 0;
  }
}

@media (max-width: 980px) {
  .past-header,
  .history-card {
    align-items: stretch;
    grid-template-columns: 1fr;
  }

  .past-header {
    display: grid;
  }

  .past-count {
    width: fit-content;
  }
}

@media (max-width: 760px) {
  .action-summary {
    grid-template-columns: 1fr;
  }

  .history-actions {
    grid-template-columns: 1fr;
  }

  .continue-action {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .past-shell {
    padding: 18px 14px 28px;
  }

  .history-main {
    grid-template-columns: 40px minmax(0, 1fr);
  }

  .history-index {
    width: 40px;
    height: 40px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .past-page *,
  .past-page *::before,
  .past-page *::after {
    animation: none !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
