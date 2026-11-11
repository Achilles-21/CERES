<script lang="ts" setup>
import usePage from '@/hooks/usePage'
import i18n from '@/lang'

const props = defineProps(['changeInfo', 'cateId'])
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { total, page } = usePage(10)
const list = ref<any[]>([])
const answerList = ref<Array<{ examId: string | number; answer: any }>>([])
const loading = ref(true)
const time = ref(0)
const completionVisible = ref(false)
const completionShown = ref(false)
let timeInterval: number | undefined
let completionTimer: number | undefined

const answeredCount = computed(() => answerList.value.length)
const questionCount = computed(() => list.value.length)
const progressText = computed(() => `${answeredCount.value}/${questionCount.value || total.value || 0}`)

const getList = async () => {
  loading.value = true
  try {
    const res = await proxy.$api.test.getExamList(page, props.cateId)
    list.value = res.data?.recommendExamRespList || []
    total.value = res.data?.total || list.value.length
    if (time.value === 0 && res.data?.limitTime) {
      time.value = Number(res.data.limitTime) * 60
      timeCalc()
    }
  } finally {
    loading.value = false
  }
}

const showCompletionReward = () => {
  if (completionShown.value || !questionCount.value) return
  completionShown.value = true
  completionVisible.value = true
  if (completionTimer) window.clearTimeout(completionTimer)
  completionTimer = window.setTimeout(() => {
    completionVisible.value = false
  }, 3000)
}

const changeStatus = async (answer: any, examId: string | number) => {
  const existingAnswer = answerList.value.find(item => item.examId == examId)
  if (existingAnswer) {
    existingAnswer.answer = answer
  } else {
    answerList.value.push({ examId, answer })
  }

  if (answeredCount.value >= questionCount.value) {
    showCompletionReward()
  }
}

const timeCalc = () => {
  if (timeInterval) window.clearInterval(timeInterval)
  timeInterval = window.setInterval(async () => {
    time.value--
    if (time.value <= 0) {
      if (timeInterval) window.clearInterval(timeInterval)
      if (answerList.value.length > 0) handle()
      else proxy.$router.back()
    }
  }, 1000)
}

const timeToSecond = (duration: number) => {
  const safeDuration = Math.max(duration, 0)
  const minute = Math.floor(safeDuration / 60)
  const second = safeDuration - minute * 60
  return (minute < 10 ? '0' + minute : minute) + ':' + (second < 10 ? '0' + second : second)
}

const handle = async () => {
  const res = await proxy.$api.test.sendAnswerList({ examRecordReqs: answerList.value, cateId: props.cateId })
  if (res.code == 200) {
    window.$msg.info(i18n.global.t('question.commitSuccess'))
    proxy.$router.back()
  }
}

const sendAnswer = async () => {
  if (answerList.value.length == 0) {
    window.$msg.error(i18n.global.t('question.commithasNone'))
  } else if (questionCount.value > answerList.value.length) {
    window.$dialog.warning({
      title: i18n.global.t('question.commitHasPart'),
      content: i18n.global.t('question.commitAsk'),
      positiveText: i18n.global.t('question.confirm'),
      negativeText: i18n.global.t('question.cancel'),
      onPositiveClick: async () => {
        await handle()
      }
    })
  } else await handle()
}

const sendQue = (ques: any[]) => {
  list.value = ques || []
  total.value = list.value.length
  if (list.value[0]?.courseId) getNoteInfo(list.value[0].courseId)
}

const getNoteInfo = async (id: string | number) => {
  const res = await proxy.$api.note.getNote({ videoId: id })
  if (res.code == 200) {
    const obj = {
      videoId: id,
      id: res.data.id,
      v: res.data.content
    }
    props.changeInfo(obj)
  } else {
    window.$msg.destroyAll()
  }
}

onMounted(() => {
  getList()
  proxy.$mitt.on('sendQue', sendQue)
})

onBeforeUnmount(() => {
  proxy.$mitt.off('sendQue', sendQue)
  if (timeInterval) window.clearInterval(timeInterval)
  if (completionTimer) window.clearTimeout(completionTimer)
})
</script>

<template>
  <section class="practice-page-shell font-cjk-page">
    <header class="practice-topbar ceres-card">
      <button class="practice-back ceres-icon-button" type="button" aria-label="返回" @click="$router.back()">
        <span class="iconify ic--baseline-keyboard-arrow-left" aria-hidden="true"></span>
      </button>

      <div class="practice-title-block">
        <p class="practice-eyebrow">
          <span class="iconify solar--checklist-minimalistic-bold" aria-hidden="true"></span>
          练习模式
        </p>
        <h1>{{ i18n.global.t('home.goPractice') }}</h1>
      </div>

      <div class="practice-summary" aria-live="polite">
        <span class="summary-pill">
          <span class="iconify solar--list-check-bold" aria-hidden="true"></span>
          {{ progressText }}
        </span>
        <span class="summary-pill is-time">
          <span class="iconify solar--clock-circle-bold" aria-hidden="true"></span>
          {{ timeToSecond(time) }}
        </span>
        <n-button type="error" strong secondary @click="sendAnswer">{{ i18n.global.t('question.commit') }}</n-button>
      </div>
    </header>

    <n-scrollbar class="practice-question-scroll">
      <div class="practice-question-list">
        <XwyaSubjectType
          v-for="(p, i) in list"
          :key="p.queId || p.id || i"
          :data="p"
          :index="i + page.pageSize * (page.pageNum - 1)"
          :showLike="false"
          :change="changeStatus"
        />

        <div v-if="!loading && !list.length" class="practice-empty ceres-card">
          <span class="iconify solar--clipboard-remove-bold" aria-hidden="true"></span>
          <n-empty :description="i18n.global.t('question.none')" />
        </div>

        <div v-if="loading" class="practice-loading ceres-card" role="status">
          <n-spin size="large" />
          <span>正在载入练习题...</span>
        </div>
      </div>
    </n-scrollbar>

    <transition name="practice-complete">
      <div v-if="completionVisible" class="practice-complete-overlay" role="status" aria-live="polite">
        <div class="complete-reward-card">
          <span class="complete-orbit orbit-one"></span>
          <span class="complete-orbit orbit-two"></span>
          <span class="complete-orbit orbit-three"></span>
          <span class="complete-trophy iconify solar--cup-star-bold" aria-hidden="true"></span>
          <strong>全部完成</strong>
          <span>保持节奏，继续巩固知识点</span>
        </div>
      </div>
    </transition>
  </section>
</template>

<style lang="css" scoped>
.practice-page-shell {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  border-radius: calc(var(--ceres-radius) + 2px);
  background:
    radial-gradient(circle at 12% 0%, rgba(34, 197, 94, 0.16), transparent 28rem),
    linear-gradient(180deg, rgba(238, 242, 255, 0.88), rgba(248, 250, 252, 0.96));
}

.practice-topbar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  flex-shrink: 0;
  align-items: center;
  gap: 14px;
  margin: 0 0 12px;
  border-color: rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px;
}

.practice-back {
  width: 42px;
  height: 42px;
  font-size: 26px;
}

.practice-title-block {
  min-width: 0;
}

.practice-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 2px;
  color: #059669;
  font-size: 12px;
  font-weight: 850;
}

.practice-title-block h1 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(20px, 2.4vw, 28px);
  font-weight: 900;
  line-height: 1.18;
}

.practice-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 9px;
}

.summary-pill {
  display: inline-flex;
  min-height: 36px;
  align-items: center;
  gap: 7px;
  border: 1px solid rgba(34, 197, 94, 0.22);
  border-radius: 999px;
  background: rgba(240, 253, 244, 0.88);
  color: #047857;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 900;
}

.summary-pill.is-time {
  border-color: rgba(248, 113, 113, 0.24);
  background: rgba(254, 242, 242, 0.9);
  color: #b91c1c;
}

.practice-question-scroll {
  min-height: 0;
  flex: 1;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.56);
}

.practice-question-list {
  min-height: 100%;
  padding: clamp(14px, 2vw, 20px);
}

.practice-empty,
.practice-loading {
  display: grid;
  min-height: 260px;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #64748b;
}

.practice-empty > .iconify {
  color: var(--ceres-primary);
  font-size: 42px;
}

.practice-loading span {
  color: #475569;
  font-weight: 800;
}

.practice-complete-overlay {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.22);
  backdrop-filter: blur(3px);
  pointer-events: none;
}

.complete-reward-card {
  position: relative;
  display: grid;
  width: min(86vw, 320px);
  place-items: center;
  gap: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(255, 251, 235, 0.98), rgba(240, 253, 244, 0.95)),
    #ffffff;
  color: #0f172a;
  padding: 30px 24px;
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.24);
  animation: reward-enter 520ms cubic-bezier(0.2, 0.9, 0.2, 1.1) both;
}

.complete-trophy {
  color: #d97706;
  font-size: 70px;
  filter: drop-shadow(0 12px 20px rgba(217, 119, 6, 0.24));
  animation: trophy-celebrate 1200ms ease-in-out infinite;
}

.complete-reward-card strong {
  font-size: 24px;
  font-weight: 950;
}

.complete-reward-card > span:last-child {
  color: #475569;
  font-size: 14px;
  font-weight: 800;
}

.complete-orbit {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #22c55e;
  opacity: 0.78;
}

.orbit-one {
  top: 26px;
  left: 46px;
  animation: orbit-one 1100ms ease-in-out infinite;
}

.orbit-two {
  top: 44px;
  right: 50px;
  background: #f59e0b;
  animation: orbit-two 1280ms ease-in-out infinite;
}

.orbit-three {
  right: 74px;
  bottom: 34px;
  background: #4f46e5;
  animation: orbit-three 1180ms ease-in-out infinite;
}

.practice-complete-enter-active,
.practice-complete-leave-active {
  transition: opacity 180ms ease;
}

.practice-complete-enter-from,
.practice-complete-leave-to {
  opacity: 0;
}

@keyframes reward-enter {
  from {
    transform: translateY(18px) scale(0.92);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

@keyframes trophy-celebrate {
  0%, 100% {
    transform: translateY(0) rotate(-2deg);
  }
  50% {
    transform: translateY(-6px) rotate(3deg);
  }
}

@keyframes orbit-one {
  50% {
    transform: translate(-8px, 10px) scale(1.4);
  }
}

@keyframes orbit-two {
  50% {
    transform: translate(9px, -8px) scale(1.3);
  }
}

@keyframes orbit-three {
  50% {
    transform: translate(8px, 9px) scale(1.25);
  }
}

@media (max-width: 720px) {
  .practice-topbar {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .practice-summary {
    grid-column: 1 / -1;
    justify-content: flex-start;
    padding-left: 56px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .practice-complete-enter-active,
  .practice-complete-leave-active,
  .complete-reward-card,
  .complete-trophy,
  .complete-orbit {
    animation: none !important;
    transition: none;
  }
}
</style>
