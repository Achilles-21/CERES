<script lang="ts" setup>
import useSystemStore from '@/store/useSystemStore'
const { proxy }: { proxy: CusProxy } = getCurrentInstance()
const list = ref<any[]>([])
const reportId = ref(0)
const flag = ref(false)
const index = ref(0)
const quelist = ref<any[]>([])
const queBg = ref<any>({})
const chartRef = ref<any>(null)
const systemStore = useSystemStore()
const activeLanguage = computed(() => String(systemStore.lang || localStorage.getItem('lang') || 'zhcn'))

const stageStep = computed(() => {
  if (!list.value.length) return 0
  if (!flag.value) return 1
  if (quelist.value.length && index.value >= quelist.value.length) return 3
  return 2
})
const currentStageLabel = computed(() => {
  if (stageStep.value === 0) return proxy.$t('case.stageWaiting')
  if (stageStep.value === 1) return proxy.$t('case.stageInquiry')
  if (stageStep.value === 2) return proxy.$t('case.stageReasoning')
  return proxy.$t('case.stageReview')
})
const stageProgress = computed(() => {
  if (stageStep.value === 0) return '18%'
  if (stageStep.value === 1) return '42%'
  if (stageStep.value === 2) return '72%'
  return '100%'
})
const stageStats = computed(() => [
  {
    label: proxy.$t('case.statMessages'),
    value: list.value.length,
    icon: 'solar--chat-round-line-bold',
    tone: 'purple'
  },
  {
    label: proxy.$t('case.statQuestions'),
    value: quelist.value.length,
    icon: 'solar--document-medicine-bold',
    tone: 'cyan'
  },
  {
    label: proxy.$t('case.statProgress'),
    value: quelist.value.length ? `${Math.min(index.value + (flag.value && index.value < quelist.value.length ? 1 : 0), quelist.value.length)}/${quelist.value.length}` : '0/0',
    icon: 'solar--flag-bold',
    tone: 'emerald'
  }
])

const getlist = async () => {
  const res = await proxy.$api.case.getAnalysisList({ language: activeLanguage.value })
  if (res.code === 200) {
    list.value = res.data
  }
}
const onEnter = async (val, _base, _id, _content, _answer, _isQue, _queId, _modelType = 0) => {
  list.value = list.value.concat([
    {
      role: 'user',
      content: val,
      temp: true
    },
    {
      role: 'system',
      content: '',
      picture: 1,
      loading: true,
      temp: true
    }
  ])
  let data = []
  if (!flag.value) {
    const res = await proxy.$api.case.getAnalysisList({ query: val, reportId: reportId.value, language: activeLanguage.value })
    if (res.code === 200) {
      data = res.data
    }
  } else {
    const res = await proxy.$api.case.getAnswer({ question: quelist.value[index.value].question, patient: quelist.value[index.value].patient, answer: quelist.value[index.value].answer, studentAnswers: val, language: activeLanguage.value })
    if (res.code === 200) {
      data = res.data
      index.value++
    }
  }
  data.forEach(item => {
    if (item.reportId) {
      reportId.value = item.reportId
    }
    for (let i = list.value.length - 1; i >= 0; i--) {
      if (list.value[i].temp) {
        list.value.splice(i, 1)
        continue
      }
    }
    list.value.push(item)
    if (item.imageUrls && item.imageUrls.length > 0) {
      item.imageUrls.forEach((itemb: any) => {
        list.value.push({ ...item, content: `<div class="n-image"><img style="width:50%" src="${itemb}" /></div>` })
      })
    }
    if (item.caseQueBackList && item.caseQueBackList.length > 0) {
      quelist.value = item.caseQueBackList
    }
    if (item.patient) {
      queBg.value = { ...list.value[0], content: item.patient }
    }
  })
}
const next = async () => {
  if (quelist.value.length === 0) {
    window.$msg.error(proxy.$t('case.tips1'))
    return
  }

  if (index.value > quelist.value.length - 1) {
    window.$msg.error(proxy.$t('case.tips2'))
    return
  }
  if (!flag.value && index.value === 0) {
    list.value.push(queBg.value)
  }
  flag.value = true

  if (!quelist.value[index.value].push) {
    const res = await proxy.$api.case.getModalQueList({ question: quelist.value[index.value].question, patient: quelist.value[index.value].patient, language: activeLanguage.value })
    if (res.code == 200) {
      quelist.value[index.value].push = true
      list.value.push(res.data)
    }
  } else {
    window.$msg.error(proxy.$t('case.tips4'))
    return
  }
}
const clearList = () => {
  list.value = []
  flag.value = false
  index.value = 0
  quelist.value = []
  queBg.value = {}
  reportId.value = 0
}
const clearConversation = () => {
  chartRef.value?.handleClearChat?.()
}
const show = async () => {
  if (index.value === 0) {
    window.$msg.error(proxy.$t('case.tips3'))
    return
  }
  const res = await proxy.$api.case.showAnswer({ id: reportId.value, question: quelist.value[index.value - 1].question })
  if (res.code === 200) {
    list.value.push(res.data)
  }
}
onMounted(() => {
  getlist()
})
</script>

<template>
  <main class="case-page ceres-gradient-bg font-latin-page">
    <section class="case-shell ceres-page-shell">
      <header class="case-header ceres-glass">
        <div class="case-title-wrap">
          <span class="case-title-icon iconify solar--medical-kit-bold"></span>
          <div>
            <p class="case-eyebrow">{{ $t('case.workspace') }}</p>
            <h1>{{ $t('case.title') }}</h1>
            <p class="case-description">{{ $t('case.description') }}</p>
          </div>
        </div>
        <div class="case-stage-badge ceres-pill">
          <span class="stage-live-dot"></span>
          {{ currentStageLabel }}
        </div>
      </header>

      <div class="case-workspace">
        <aside class="case-stage-panel ceres-card">
          <div class="stage-panel-header">
            <div>
              <p class="panel-kicker">{{ $t('case.stageGuide') }}</p>
              <h2>{{ $t('case.stageTitle') }}</h2>
            </div>
            <span class="panel-icon iconify solar--routing-3-bold"></span>
          </div>

          <div class="stage-progress-track" aria-hidden="true">
            <span :style="{ width: stageProgress }"></span>
          </div>

          <ol class="stage-list">
            <li :class="{ 'is-active': stageStep <= 1, 'is-done': stageStep > 1 }">
              <span class="stage-index">1</span>
              <div>
                <h3>{{ $t('case.stageOneTitle') }}</h3>
                <p>{{ $t('case.stageOneDesc') }}</p>
              </div>
            </li>
            <li :class="{ 'is-active': stageStep === 2, 'is-done': stageStep > 2 }">
              <span class="stage-index">2</span>
              <div>
                <h3>{{ $t('case.stageTwoTitle') }}</h3>
                <p>{{ $t('case.stageTwoDesc') }}</p>
              </div>
            </li>
            <li :class="{ 'is-active': stageStep === 3 }">
              <span class="stage-index">3</span>
              <div>
                <h3>{{ $t('case.stageThreeTitle') }}</h3>
                <p>{{ $t('case.stageThreeDesc') }}</p>
              </div>
            </li>
          </ol>

          <div class="stage-stat-grid">
            <div v-for="item in stageStats" :key="item.label" class="stage-stat" :class="`tone-${item.tone}`">
              <span class="iconify" :class="item.icon"></span>
              <div>
                <p>{{ item.label }}</p>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>

          <div class="stage-actions-card">
            <p class="actions-title">{{ $t('case.currentAction') }}</p>
            <button type="button" class="stage-action primary" @click="next">
              <span class="iconify solar--arrow-right-up-bold"></span>
              {{ $t('case.bnt2') }}
            </button>
            <button type="button" class="stage-action secondary" @click="show">
              <span class="iconify solar--eye-bold"></span>
              {{ $t('case.bnt1') }}
            </button>
            <n-popconfirm
              :positive-text="$t('system.clearConfirm')"
              :negative-text="$t('system.clearCancel')"
              @positive-click="clearConversation"
            >
              {{ $t('system.confirmClearChat') }}
              <template #trigger>
                <button type="button" class="stage-action danger">
                  <span class="iconify ic--baseline-delete-outline"></span>
                  {{ $t('system.clearChat') }}
                </button>
              </template>
            </n-popconfirm>
          </div>
        </aside>

        <section class="case-chat-panel">
          <XwyaChart ref="chartRef" :size="16" :msgList="list" :show="show" :onEnter="onEnter" :next="next" :isBnt="false" :onClear="clearList" />
        </section>
      </div>
    </section>
  </main>
</template>

<style lang="css" scoped>
.case-page {
  min-height: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-gutter: stable;
}


.case-shell {
  display: flex;
  min-height: max(100%, 980px);
  height: auto;
  flex-direction: column;
  gap: 16px;
}


.case-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
}

.case-title-wrap {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.case-title-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-cyan));
  color: #fff;
  font-size: 26px;
  box-shadow: 0 18px 38px rgba(var(--ceres-primary-rgb), 0.24);
}

.case-eyebrow,
.panel-kicker {
  color: var(--ceres-muted);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.case-title-wrap h1 {
  margin: 2px 0 4px;
  color: var(--ceres-fg);
  font-size: clamp(23px, 2.6vw, 31px);
  line-height: 1.12;
}

.case-description {
  max-width: 760px;
  color: var(--ceres-muted);
  font-size: 13px;
  line-height: 1.55;
}

.case-stage-badge {
  min-height: 36px;
  flex-shrink: 0;
  padding: 5px 12px;
  color: var(--ceres-primary);
  font-size: 13px;
}

.stage-live-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--ceres-emerald);
  box-shadow: 0 0 0 4px rgba(5, 150, 105, 0.12);
}

.case-workspace {
  display: grid;
  min-height: 0;
  flex: 1;
  grid-template-columns: minmax(260px, 330px) minmax(0, 1fr);
  gap: 18px;
}

.case-stage-panel {
  display: flex;
  min-height: 0;
  flex-direction: column;
  gap: 16px;
  overflow: auto;
  padding: 16px;
}

.stage-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stage-panel-header h2 {
  margin-top: 1px;
  color: var(--ceres-fg);
  font-size: 18px;
}

.panel-icon {
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--ceres-primary-soft);
  color: var(--ceres-primary);
  font-size: 20px;
}

.stage-progress-track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(var(--ceres-primary-rgb), 0.1);
}


.stage-progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--ceres-primary), var(--ceres-cyan), var(--ceres-emerald));
  transition: width 240ms ease;
}

.stage-list {
  display: grid;
  gap: 9px;
  list-style: none;
}

.stage-list li {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  gap: 10px;
  border: 1px solid var(--ceres-border);
  border-radius: 14px;
  background: rgba(var(--ceres-card-rgb), 0.72);
  padding: 9px 10px;
  transition: border-color 200ms ease, background-color 200ms ease, box-shadow 200ms ease;
}

.stage-list li.is-active {
  border-color: rgba(var(--ceres-primary-rgb), 0.34);
  background: var(--ceres-primary-soft);
  box-shadow: 0 12px 24px rgba(var(--ceres-primary-rgb), 0.1);
}

.stage-list li.is-done {
  border-color: rgba(5, 150, 105, 0.26);
}

.stage-index {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(var(--ceres-primary-rgb), 0.1);
  color: var(--ceres-primary);
  font-size: 13px;
  font-weight: 900;
}

.stage-list h3 {
  margin: 0 0 4px;
  color: var(--ceres-fg);
  font-size: 13px;
}

.stage-list p {
  color: var(--ceres-muted);
  font-size: 12px;
  line-height: 1.55;
}

.stage-stat-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.stage-stat {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--ceres-border);
  border-radius: 12px;
  background: rgba(var(--ceres-card-rgb), 0.78);
  padding: 9px 10px;
}


.stage-stat .iconify {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: var(--tone-color, var(--ceres-primary));
}

.stage-stat p {
  color: var(--ceres-muted);
  font-size: 11px;
  font-weight: 700;
}

.stage-stat strong {
  display: block;
  margin-top: 1px;
  color: var(--ceres-fg);
  font-size: 15px;
}

.tone-purple {
  --tone-color: var(--ceres-primary);
}

.tone-cyan {
  --tone-color: var(--ceres-cyan);
}

.tone-emerald {
  --tone-color: var(--ceres-emerald);
}

.stage-actions-card {
  display: grid;
  gap: 9px;
  margin-top: auto;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(var(--ceres-primary-rgb), 0.08), rgba(8, 145, 178, 0.07));
  padding: 12px;
}

.actions-title {
  color: var(--ceres-muted);
  font-size: 12px;
  font-weight: 900;
}

.stage-action {
  display: inline-flex;
  min-height: 38px;
  width: 100%;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 7px 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 900;
  transition: border-color 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.stage-action.primary {
  background: linear-gradient(135deg, var(--ceres-primary), var(--ceres-primary-hover));
  color: #fff;
  box-shadow: 0 12px 24px rgba(var(--ceres-primary-rgb), 0.22);
}

.stage-action.secondary {
  border-color: rgba(var(--ceres-primary-rgb), 0.24);
  background: rgba(var(--ceres-card-rgb), 0.9);
  color: var(--ceres-primary);
}

.stage-action.danger {
  border-color: rgba(220, 38, 38, 0.22);
  background: rgba(var(--ceres-card-rgb), 0.9);
  color: var(--ceres-danger);
}

.stage-action:hover,
.stage-action:focus-visible {
  border-color: rgba(var(--ceres-primary-rgb), 0.42);
  box-shadow: 0 10px 24px rgba(var(--ceres-primary-rgb), 0.14);
  outline: none;
}

.case-chat-panel {
  min-height: 0;
}

.case-chat-panel :deep(.ai-chat-shell) {
  min-height: 0;
}

.case-chat-panel :deep(.chat-panel-header) {
  padding: 11px 14px;
}

.case-chat-panel :deep(.assistant-title-block) {
  gap: 10px;
}

.case-chat-panel :deep(.assistant-icon) {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  font-size: 20px;
}

.case-chat-panel :deep(.assistant-kicker) {
  font-size: 11px;
}

.case-chat-panel :deep(.assistant-title-block h2) {
  font-size: 16px;
}

.case-chat-panel :deep(.assistant-status) {
  min-height: 30px;
  padding: 3px 10px;
  font-size: 12px;
}

.case-chat-panel :deep(.message-list) {
  gap: 12px;
  padding: 14px 16px;
}

.case-chat-panel :deep(.message-row) {
  gap: 9px;
}

.case-chat-panel :deep(.message-avatar) {
  width: 32px;
  height: 32px;
}

.case-chat-panel :deep(.message-body) {
  max-width: min(820px, calc(100% - 41px));
  gap: 6px;
}

.case-chat-panel :deep(.message-meta) {
  gap: 6px;
  font-size: 11px;
}

.case-chat-panel :deep(.message-badge) {
  min-height: 20px;
  padding: 1px 7px;
}

.case-chat-panel :deep(.message-content-card) {
  border-radius: 15px;
  padding: 10px 13px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.case-chat-panel :deep(.text-content) {
  line-height: 1.62;
}

.case-chat-panel :deep(.text-content p) {
  margin-bottom: 8px;
}

.case-chat-panel :deep(.prompt-footer) {
  padding: 10px;
}

.case-chat-panel :deep(.prompt-card) {
  min-height: 168px;
  gap: 10px;
  border-radius: 16px;
  padding: 10px;
}

.case-chat-panel :deep(.prompt-editor) {
  border-radius: 13px;
  padding: 10px;
}

.case-chat-panel :deep(.input-text-content) {
  min-height: 88px;
  max-height: 132px;
  font-size: 14px;
}

.case-chat-panel :deep(.prompt-button-row) {
  gap: 10px;
}

.case-chat-panel :deep(.prompt-tools),
.case-chat-panel :deep(.prompt-actions) {
  gap: 7px;
}

.case-chat-panel :deep(.prompt-icon-button) {
  width: 34px;
  height: 34px;
  font-size: 18px;
}


@media (max-width: 1024px) {
  .case-page {
    height: 100%;
    min-height: 100%;
    overflow-x: hidden;
    overflow-y: auto;
  }

  .case-shell {
    min-height: auto;
    height: auto;
  }

  .case-workspace {
    grid-template-columns: 1fr;
  }

  .case-stage-panel,
  .case-chat-panel :deep(.ai-chat-shell) {
    min-height: 620px;
  }
}

@media (max-width: 768px) {
  .case-header,
  .case-title-wrap {
    align-items: flex-start;
  }

  .case-header {
    flex-direction: column;
  }

  .case-stage-badge {
    width: fit-content;
  }

  .stage-stat-grid {
    grid-template-columns: 1fr;
  }

  .case-chat-panel :deep(.chat-panel-header) {
    gap: 10px;
  }

  .case-chat-panel :deep(.message-list) {
    padding: 12px;
  }

  .case-chat-panel :deep(.message-body) {
    max-width: calc(100% - 40px);
  }

  .case-chat-panel :deep(.prompt-card) {
    min-height: 154px;
  }

  .case-chat-panel :deep(.input-text-content) {
    min-height: 76px;
  }
}

@media (prefers-reduced-motion: reduce) {

  .stage-progress-track span,
  .stage-list li,
  .stage-action {
    transition: none;
  }
}
</style>
