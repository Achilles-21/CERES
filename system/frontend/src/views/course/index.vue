<script lang="ts" setup>
import usePage from '@/hooks/usePage'

const { page, total, loading, data } = usePage(8)
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { push } = useRouter()

const pageConfig = computed(() => ({
  itemCount: total.value,
  pageSizes: [8, 12, 20],
  pageSlot: 7,
  pageSize: page.pageSize,
  page: page.pageNum,
  'onUpdate:page': (p: number) => {
    page.pageNum = p
    getList()
  },
  'onUpdate:pageSize': (p: number) => {
    page.pageSize = p
    page.pageNum = 1
    getList()
  }
}))

const getList = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.video.getCourseList(page)
    total.value = res.data?.total || 0
    data.value = res.data?.row || []
  } finally {
    loading.value = false
  }
}

const pushPage = async (id: number | string) => {
  proxy.$api.video.addVideoRate({ id })
  push('/study?id=' + id)
}

const hitCount = (hits: number | string | undefined) => Number(hits || 0)

onMounted(() => {
  getList()
})
</script>

<template>
  <main class="course-page ceres-gradient-bg">
    <section class="course-shell ceres-page-shell">
      <header class="course-header">
        <div>
          <h1>{{ $t('home.goToClass') }}</h1>
          <p>{{ $t('video.continue') }}</p>
        </div>
      </header>

      <div v-if="loading" class="course-grid" aria-busy="true">
        <div v-for="item in 8" :key="item" class="course-card course-skeleton ceres-card">
          <div class="skeleton-cover"></div>
          <div class="skeleton-body">
            <span></span>
            <span></span>
          </div>
        </div>
      </div>

      <div v-else-if="data.length" class="course-grid">
        <button v-for="p in data" :key="p.id" class="course-card ceres-card" type="button" @click="pushPage(p.id)">
          <span class="course-cover">
            <img :src="p.posterUrl || '/maxLogo.svg'" :alt="p.name || $t('video.videoCover')" />
            <span class="course-overlay">
              <span>{{ $t('video.continue') }}</span>
              <span class="iconify solar--arrow-right-linear" aria-hidden="true"></span>
            </span>
          </span>
          <span class="course-content">
            <span class="course-title">{{ p.name }}</span>
            <span class="course-meta">
              <span class="course-hits" :aria-label="String(hitCount(p.hits))">
                <span class="iconify gg--eye-alt" aria-hidden="true"></span>
                <span>{{ hitCount(p.hits) }}</span>
              </span>
            </span>
          </span>
        </button>
      </div>

      <div v-else class="empty-card ceres-card">
        <span class="iconify solar--folder-open-bold" aria-hidden="true"></span>
        <h2>{{ $t('system.nodata') }}</h2>
        <p>{{ $t('video.courseNameTips') }}</p>
      </div>

      <div v-if="total > page.pageSize" class="course-pagination">
        <n-pagination v-bind="pageConfig" />
      </div>
    </section>
  </main>
</template>

<style lang="css" scoped>
.course-page {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  color: #0f172a;
}

.course-shell {
  display: flex;
  width: 100%;
  max-width: 1180px;
  min-height: 100%;
  flex: 1;
  flex-direction: column;
  margin: 0 auto;
  padding: clamp(18px, 3vw, 38px);
}

.course-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  animation: fade-up 240ms ease both;
}

.course-header h1 {
  margin: 0 0 8px;
  color: #111827;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 850;
  line-height: 1.16;
}

.course-header p {
  margin: 0;
  color: #475569;
  font-size: 16px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  align-content: start;
  gap: 18px;
  animation: fade-up 260ms ease both;
}

.course-card {
  display: flex;
  min-width: 0;
  cursor: pointer;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 0;
  text-align: left;
  transition: border-color 180ms ease, box-shadow 200ms ease, transform 200ms ease;
}

.course-card:hover {
  border-color: rgba(114, 46, 209, 0.48);
  box-shadow: 0 22px 46px rgba(30, 41, 59, 0.14);
  transform: translateY(-2px);
}

.course-card:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.32);
  outline-offset: 4px;
}

.course-cover {
  position: relative;
  display: block;
  aspect-ratio: 16 / 9;
  width: 100%;
  overflow: hidden;
  background: #eef2ff;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: filter 200ms ease;
}

.course-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, rgba(17, 24, 39, 0.72), rgba(114, 46, 209, 0.68));
  color: #fff;
  font-weight: 800;
  opacity: 0;
  transition: opacity 180ms ease;
}

.course-card:hover .course-overlay,
.course-card:focus-visible .course-overlay {
  opacity: 1;
}

.course-card:hover .course-cover img {
  filter: saturate(1.08) contrast(1.04);
}

.course-content {
  display: flex;
  min-height: 122px;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
}

.course-title {
  display: -webkit-box;
  overflow: hidden;
  color: #111827;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.course-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.course-hits {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #64748b;
  font-weight: 700;
}

.course-pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0 6px;
}

.course-pagination :deep(.n-pagination) {
  flex-wrap: wrap;
  justify-content: center;
}

.empty-card {
  display: flex;
  min-height: 260px;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px;
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
  margin: 0;
}

.course-skeleton {
  min-height: 288px;
  cursor: default;
}

.skeleton-cover,
.skeleton-body span {
  background: linear-gradient(90deg, rgba(226, 232, 240, 0.75), rgba(241, 245, 249, 0.95), rgba(226, 232, 240, 0.75));
  background-size: 220% 100%;
  animation: shimmer 1100ms ease-in-out infinite;
}

.skeleton-cover {
  aspect-ratio: 16 / 9;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.skeleton-body span {
  height: 16px;
  border-radius: 999px;
}

.skeleton-body span:last-child {
  width: 58%;
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

@media (hover: none) {
  .course-overlay {
    align-items: flex-end;
    justify-content: flex-start;
    padding: 14px;
    background: linear-gradient(180deg, transparent, rgba(17, 24, 39, 0.72));
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .course-shell {
    padding: 18px 14px 28px;
  }

  .course-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .course-meta {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .course-page *,
  .course-page *::before,
  .course-page *::after {
    animation: none !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
