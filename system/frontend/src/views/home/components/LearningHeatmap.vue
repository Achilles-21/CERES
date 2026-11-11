<script lang="ts" setup>
import { getDurationHeatmap } from '@/api/system'
import useSystemStore from '@/store/useSystemStore'

type HeatmapDay = {
  day: string
  level: number
  score: number
  typeCount: number
  sessionCount: number
  typeCounts?: Record<string, number>
}

type HeatmapPayload = {
  startDay: string
  endDay: string
  totalTypes: number
  activeDays: number
  currentStreak: number
  maxStreak: number
  totalSessions: number
  days: HeatmapDay[]
}

type CalendarCell = HeatmapDay & {
  date: Date
}

const systemStore = useSystemStore()
const loading = ref(true)
const error = ref('')
const heatmap = ref<HeatmapPayload | null>(null)

const currentLang = computed(() => String(systemStore.lang || '').toLowerCase())
const isEnglish = computed(() => currentLang.value.startsWith('en'))
const isMalay = computed(() => currentLang.value.includes('malay') || currentLang.value.startsWith('ms'))
const isHindi = computed(() => currentLang.value.includes('hindi') || currentLang.value.startsWith('hi'))

const copy = computed(() => {
  if (isEnglish.value) {
    return {
      description: 'Daily interaction breadth and frequency across CERES learning modules.',
      activeDays: 'Active days',
      currentStreak: 'Current streak',
      maxStreak: 'Best streak',
      sessions: 'Interactions',
      loading: 'Loading learning activity',
      empty: 'No tracked learning activity yet. Start any module to light up the calendar.',
      error: 'Learning activity could not be loaded.',
      less: 'Less',
      more: 'More',
      level: 'level',
      types: 'types',
      interactions: 'interactions',
      retry: 'Retry',
      weekdays: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
      months: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    }
  }

  if (isMalay.value) {
    return {
      description: 'Kepelbagaian dan kekerapan interaksi harian merentas modul CERES.',
      activeDays: 'Hari aktif',
      currentStreak: 'Rentetan kini',
      maxStreak: 'Rentetan terbaik',
      sessions: 'Interaksi',
      loading: 'Memuatkan aktiviti pembelajaran',
      empty: 'Belum ada aktiviti pembelajaran direkodkan. Mulakan mana-mana modul untuk mengisi kalendar.',
      error: 'Aktiviti pembelajaran tidak dapat dimuatkan.',
      less: 'Kurang',
      more: 'Lebih',
      level: 'tahap',
      types: 'jenis',
      interactions: 'interaksi',
      retry: 'Cuba lagi',
      weekdays: ['Aha', 'Isn', 'Sel', 'Rab', 'Kha', 'Jum', 'Sab'],
      months: ['Jan', 'Feb', 'Mac', 'Apr', 'Mei', 'Jun', 'Jul', 'Ogo', 'Sep', 'Okt', 'Nov', 'Dis']
    }
  }

  if (isHindi.value) {
    return {
      description: 'CERES मॉड्यूल में दैनिक इंटरैक्शन की विविधता और आवृत्ति।',
      activeDays: 'सक्रिय दिन',
      currentStreak: 'वर्तमान क्रम',
      maxStreak: 'श्रेष्ठ क्रम',
      sessions: 'इंटरैक्शन',
      loading: 'सीखने की गतिविधि लोड हो रही है',
      empty: 'अभी कोई सीखने की गतिविधि दर्ज नहीं है। कैलेंडर भरने के लिए कोई मॉड्यूल शुरू करें।',
      error: 'सीखने की गतिविधि लोड नहीं हो सकी।',
      less: 'कम',
      more: 'अधिक',
      level: 'स्तर',
      types: 'प्रकार',
      interactions: 'इंटरैक्शन',
      retry: 'फिर कोशिश करें',
      weekdays: ['रवि', 'सोम', 'मंगल', 'बुध', 'गुरु', 'शुक्र', 'शनि'],
      months: ['जन', 'फ़र', 'मार्च', 'अप्रै', 'मई', 'जून', 'जुल', 'अग', 'सित', 'अक्टू', 'नव', 'दिस']
    }
  }

  return {
    description: '按天汇总你在 CERES 各学习模块中的交互种类和频率。',
    activeDays: '活跃天数',
    currentStreak: '连续学习',
    maxStreak: '最佳连续',
    sessions: '交互次数',
    loading: '正在加载学习活动',
    empty: '暂无学习活动记录，进入任意学习模块后会点亮日历。',
    error: '学习活动暂时加载失败。',
    less: '较少',
    more: '较多',
    level: '等级',
    types: '种类',
    interactions: '交互',
    retry: '重试',
    weekdays: ['日', '一', '二', '三', '四', '五', '六'],
    months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  }
})

const parseDay = (day: string) => {
  const [year, month, date] = day.split('-').map(Number)
  return new Date(year, month - 1, date)
}

const normalizeLevel = (level: number) => Math.max(0, Math.min(4, Number(level) || 0))

const readPayload = (payload: any): HeatmapPayload => {
  const data = payload?.data && typeof payload.data === 'object' ? payload.data : payload
  const days = Array.isArray(data?.days) ? data.days : []
  return {
    startDay: String(data?.startDay || ''),
    endDay: String(data?.endDay || ''),
    totalTypes: Number(data?.totalTypes || 6),
    activeDays: Number(data?.activeDays || 0),
    currentStreak: Number(data?.currentStreak || 0),
    maxStreak: Number(data?.maxStreak || 0),
    totalSessions: Number(data?.totalSessions || 0),
    days: days.map((item: any) => ({
      day: String(item?.day || ''),
      level: normalizeLevel(Number(item?.level || 0)),
      score: Number(item?.score || 0),
      typeCount: Number(item?.typeCount || 0),
      sessionCount: Number(item?.sessionCount || 0),
      typeCounts: item?.typeCounts && typeof item.typeCounts === 'object' ? item.typeCounts : {}
    })).filter((item: HeatmapDay) => /^\d{4}-\d{2}-\d{2}$/.test(item.day))
  }
}

const fetchHeatmap = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await getDurationHeatmap({ days: 365 })
    if (response instanceof Error) throw response
    heatmap.value = readPayload(response)
  } catch (err: any) {
    error.value = err?.message || copy.value.error
  } finally {
    loading.value = false
  }
}

const cells = computed<CalendarCell[]>(() =>
  (heatmap.value?.days || []).map(day => ({
    ...day,
    date: parseDay(day.day)
  }))
)

const weeks = computed<(CalendarCell | null)[][]>(() => {
  if (!cells.value.length) return []
  const padded: (CalendarCell | null)[] = Array.from({ length: cells.value[0].date.getDay() }, () => null)
  padded.push(...cells.value)

  const result: (CalendarCell | null)[][] = []
  for (let index = 0; index < padded.length; index += 7) {
    const week = padded.slice(index, index + 7)
    while (week.length < 7) week.push(null)
    result.push(week)
  }
  return result
})

const monthLabels = computed(() => {
  let previousMonth = -1
  return weeks.value.map(week => {
    const firstDay = week.find(Boolean) as CalendarCell | undefined
    if (!firstDay) return ''
    const month = firstDay.date.getMonth()
    if (month === previousMonth) return ''
    previousMonth = month
    return copy.value.months[month]
  })
})

const stats = computed(() => [
  { label: copy.value.activeDays, value: heatmap.value?.activeDays || 0 },
  { label: copy.value.currentStreak, value: heatmap.value?.currentStreak || 0 },
  { label: copy.value.maxStreak, value: heatmap.value?.maxStreak || 0 },
  { label: copy.value.sessions, value: heatmap.value?.totalSessions || 0 }
])

const isEmpty = computed(() => Boolean(heatmap.value && (heatmap.value.totalSessions || 0) === 0))

const describeDay = (day: CalendarCell) =>
  `${day.day}: ${copy.value.level} ${day.level}, ${day.sessionCount} ${copy.value.interactions}, ${day.typeCount}/${heatmap.value?.totalTypes || 6} ${copy.value.types}`

onMounted(fetchHeatmap)
</script>

<template>
  <section class="learning-heatmap ceres-card ceres-glass" :aria-label="copy.description">
    <header class="heatmap-header">
      <div>
        <div class="heatmap-eyebrow ceres-pill" aria-hidden="true">
          <span class="iconify solar--calendar-mark-bold" aria-hidden="true"></span>
        </div>
        <p>{{ copy.description }}</p>
      </div>

      <button v-if="error" class="heatmap-retry ceres-pill" type="button" @click="fetchHeatmap">
        <span class="iconify solar--refresh-linear" aria-hidden="true"></span>
        <span>{{ copy.retry }}</span>
      </button>
    </header>

    <div class="heatmap-stats" aria-live="polite">
      <div v-for="item in stats" :key="item.label" class="heatmap-stat">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <div v-if="loading" class="heatmap-state" role="status">
      <span class="loading-dots" aria-hidden="true"><span></span><span></span><span></span></span>
      <span>{{ copy.loading }}</span>
    </div>

    <div v-else-if="error" class="heatmap-state is-error" role="alert">
      {{ copy.error }}
    </div>

    <template v-else>
      <p v-if="isEmpty" class="heatmap-empty">{{ copy.empty }}</p>

      <div v-if="weeks.length" class="heatmap-scroll" tabindex="0" :aria-label="copy.description">
        <div class="heatmap-months" :style="{ gridTemplateColumns: `repeat(${weeks.length}, 14px)` }" aria-hidden="true">
          <span v-for="(month, index) in monthLabels" :key="`${month}-${index}`">{{ month }}</span>
        </div>

        <div class="heatmap-calendar">
          <div class="heatmap-weekdays" aria-hidden="true">
            <span v-for="day in copy.weekdays" :key="day">{{ day }}</span>
          </div>

          <div class="heatmap-weeks" :style="{ gridTemplateColumns: `repeat(${weeks.length}, 14px)` }">
            <div v-for="(week, weekIndex) in weeks" :key="weekIndex" class="heatmap-week">
              <template v-for="(day, dayIndex) in week" :key="`${weekIndex}-${dayIndex}`">
                <span v-if="!day" class="heatmap-cell is-placeholder" aria-hidden="true"></span>
                <span v-else class="heatmap-cell" :class="`is-level-${day.level}`" :title="describeDay(day)" :aria-label="describeDay(day)"></span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="heatmap-legend" aria-label="Learning activity color legend">
        <span>{{ copy.less }}</span>
        <span v-for="level in [0, 1, 2, 3, 4]" :key="level" class="heatmap-cell" :class="`is-level-${level}`" :aria-label="`${copy.level} ${level}`"></span>
        <span>{{ copy.more }}</span>
      </div>
    </template>
  </section>
</template>

<style lang="css" scoped>
.learning-heatmap {
  display: grid;
  gap: 18px;
  margin-top: clamp(22px, 3vw, 34px);
  padding: clamp(18px, 3vw, 24px);
  overflow: hidden;
}

.heatmap-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.heatmap-eyebrow {
  display: inline-flex;
  width: 34px;
  min-height: 34px;
  align-items: center;
  justify-content: center;
  padding: 0;
  color: var(--ceres-primary);
  font-size: 13px;
}

.heatmap-header p {
  max-width: 680px;
  margin: 12px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
}

.heatmap-retry {
  flex: 0 0 auto;
  padding: 0 14px;
  cursor: pointer;
}

.heatmap-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.heatmap-stat {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.13);
  border-radius: 12px;
  background: rgba(var(--ceres-card-rgb), 0.62);
}

.heatmap-stat strong,
.heatmap-stat span {
  display: block;
}

.heatmap-stat strong {
  color: #111827;
  font-size: 22px;
  font-weight: 900;
  line-height: 1.1;
}

.heatmap-stat span {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.heatmap-state,
.heatmap-empty {
  display: flex;
  min-height: 96px;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px dashed rgba(var(--ceres-primary-rgb), 0.22);
  border-radius: 12px;
  background: rgba(var(--ceres-card-rgb), 0.56);
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
}

.heatmap-state.is-error {
  border-color: rgba(220, 38, 38, 0.28);
  color: var(--ceres-danger);
}

.heatmap-empty {
  min-height: auto;
  margin: 0;
  padding: 12px 14px;
  justify-content: flex-start;
  text-align: left;
}

.heatmap-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px 2px 8px;
  scrollbar-color: rgba(var(--ceres-primary-rgb), 0.32) transparent;
}

.heatmap-scroll:focus-visible {
  outline: 3px solid var(--ceres-ring);
  outline-offset: 3px;
  border-radius: 10px;
}

.heatmap-months {
  display: grid;
  width: max-content;
  gap: 4px;
  margin-left: 38px;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

.heatmap-months span {
  min-width: 0;
  white-space: nowrap;
}

.heatmap-calendar {
  display: flex;
  width: max-content;
  gap: 8px;
  align-items: flex-start;
}

.heatmap-weekdays,
.heatmap-week {
  display: grid;
  grid-template-rows: repeat(7, 14px);
  gap: 4px;
}

.heatmap-weekdays {
  width: 30px;
  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  line-height: 14px;
  text-align: right;
}

.heatmap-weeks {
  display: grid;
  gap: 4px;
}

.heatmap-cell {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 1px solid rgba(109, 40, 217, 0.16);
  border-radius: 4px;
  background: #f4f0ff;
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.heatmap-cell:not(.is-placeholder):hover {
  border-color: rgba(var(--ceres-primary-rgb), 0.72);
  box-shadow: 0 0 0 3px rgba(var(--ceres-primary-rgb), 0.16);
}

.heatmap-cell.is-placeholder {
  border-color: transparent;
  background: transparent;
}

.heatmap-cell.is-level-0 {
  background: #f4f0ff;
}

.heatmap-cell.is-level-1 {
  background: #e9ddff;
}

.heatmap-cell.is-level-2 {
  background: #c4a7ff;
}

.heatmap-cell.is-level-3 {
  background: #8b5cf6;
}

.heatmap-cell.is-level-4 {
  background: #5b21b6;
  border-color: rgba(var(--ceres-primary-rgb), 0.48);
}

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

:global(.home-launchpad.is-dark) .heatmap-stat strong {
  color: #f8fafc;
}

:global(.home-launchpad.is-dark) .heatmap-header p,
:global(.home-launchpad.is-dark) .heatmap-stat span,
:global(.home-launchpad.is-dark) .heatmap-months,
:global(.home-launchpad.is-dark) .heatmap-weekdays,
:global(.home-launchpad.is-dark) .heatmap-legend,
:global(.home-launchpad.is-dark) .heatmap-empty,
:global(.home-launchpad.is-dark) .heatmap-state {
  color: #e2e8f0;
}

:global(.home-launchpad.is-dark) .heatmap-stat,
:global(.home-launchpad.is-dark) .heatmap-state,
:global(.home-launchpad.is-dark) .heatmap-empty {
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(23, 24, 42, 0.58);
}

:global(.home-launchpad.is-dark) .heatmap-cell.is-level-0 {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(237, 233, 254, 0.12);
}

:global(.home-launchpad.is-dark) .heatmap-cell.is-level-1 {
  background: rgba(221, 214, 254, 0.24);
}

:global(.home-launchpad.is-dark) .heatmap-cell.is-level-2 {
  background: rgba(196, 181, 253, 0.48);
}

:global(.home-launchpad.is-dark) .heatmap-cell.is-level-3 {
  background: rgba(139, 92, 246, 0.78);
}

:global(.home-launchpad.is-dark) .heatmap-cell.is-level-4 {
  background: #6d28d9;
  border-color: rgba(196, 181, 253, 0.42);
}

@media (max-width: 760px) {
  .heatmap-header {
    flex-direction: column;
  }

  .heatmap-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .heatmap-legend {
    justify-content: flex-start;
  }
}

@media (max-width: 420px) {
  .heatmap-stats {
    grid-template-columns: 1fr;
  }
}
</style>
