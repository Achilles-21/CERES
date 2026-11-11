<script lang="ts" setup>
import i18n from '@/lang'
import { marked } from 'marked'
import '@/utils/mermaid'
import { renderMermaidIn } from '@/utils/mermaid'

type AnswerValue = string | number | Array<string | number>
type QuestionOption = {
  title: string | number
  choice?: string
  id?: string | number
}
type QuestionData = {
  answer?: AnswerValue
  answerExplain?: string
  content?: string
  disabled?: boolean
  favoriteFlag?: string
  id?: string | number
  imgUrl?: string
  isanswer?: AnswerValue
  option?: QuestionOption[]
  options?: QuestionOption[]
  queId?: string | number
  title?: string
  type?: string | number
}

const props = defineProps({
  data: {
    type: Object as PropType<QuestionData>,
    default: () => ({})
  },
  index: {
    type: Number,
    default: 0
  },
  change: {
    type: Function,
    default: () => {}
  },
  like: {
    type: Function,
    default: () => {}
  },
  showLike: {
    type: Boolean,
    default: true
  },
  hideInputer: {
    type: Boolean,
    default: false
  }
})

const multiAnswer = ref<Array<string | number>>([])
const textAnswer = ref('')
const feedbackVisible = ref(false)
const analysisRef = ref<HTMLElement | null>(null)
let feedbackTimer: number | undefined

const questionType = computed(() => String(props.data.type ?? '0'))
const questionId = computed(() => props.data.queId || props.data.id)
const hasAnswered = computed(() => hasAnswerValue(props.data.isanswer))
const isCorrect = computed(() => hasAnswered.value && checkAnswer(props.data.isanswer, props.data.answer))
const cardStateClass = computed(() => (hasAnswered.value ? (isCorrect.value ? 'is-correct' : 'is-wrong') : ''))
const statusLabel = computed(() => {
  if (!hasAnswered.value) return '待作答'
  return isCorrect.value ? '答对了' : '再想想'
})
const statusIcon = computed(() => {
  if (!hasAnswered.value) return 'solar--clipboard-heart-linear'
  return isCorrect.value ? 'solar--check-circle-bold' : 'solar--sad-circle-bold'
})
const feedbackIcon = computed(() => (isCorrect.value ? 'solar--star-shine-bold' : 'solar--sad-circle-bold'))
const feedbackText = computed(() => (isCorrect.value ? '回答正确' : '回答有误'))

const normalizeAnswer = (value: AnswerValue | undefined) => {
  if (Array.isArray(value)) return value.map(item => String(item).trim()).filter(Boolean).sort()
  if (value === undefined || value === null) return []
  const stringValue = String(value).trim()
  return stringValue ? [stringValue] : []
}

function hasAnswerValue(value: AnswerValue | undefined) {
  return normalizeAnswer(value).length > 0
}

function checkAnswer(userAnswer: AnswerValue | undefined, correctAnswer: AnswerValue | undefined) {
  const user = normalizeAnswer(userAnswer)
  const correct = normalizeAnswer(correctAnswer)
  if (!user.length || !correct.length || user.length !== correct.length) return false
  return user.every((item, index) => item === correct[index])
}

const triggerFeedback = () => {
  if (feedbackTimer) window.clearTimeout(feedbackTimer)
  feedbackVisible.value = true
  feedbackTimer = window.setTimeout(() => {
    feedbackVisible.value = false
  }, 1400)
}

const commitAnswer = (value: AnswerValue) => {
  props.data.isanswer = value
  props.data.disabled = true
  props.change(value, questionId.value)
  triggerFeedback()
}

const onChecked = (v: string | number) => {
  commitAnswer(v)
}
const onLike = () => {
  props.like(props.index)
}
const changeStatus = () => {
  if (!multiAnswer.value.length) return
  commitAnswer([...multiAnswer.value])
}
const onTextChanged = () => {
  if (!textAnswer.value.trim()) return
  commitAnswer(textAnswer.value.trim())
}

watch(
  () => props.data.isanswer,
  (answer) => {
    if (Array.isArray(answer)) {
      multiAnswer.value = answer
      return
    }
    if (typeof answer === 'string' || typeof answer === 'number') {
      textAnswer.value = String(answer)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (feedbackTimer) window.clearTimeout(feedbackTimer)
})

watch(
  () => props.data.answerExplain,
  () => {
    nextTick(() => renderMermaidIn(analysisRef.value))
  }
)
</script>

<template>
  <article class="question-card-state" :class="cardStateClass">
    <header class="question-card-header">
      <div class="question-number">{{ index + 1 }}</div>
      <div class="question-heading">
        <p class="question-type-label">
          <span class="iconify solar--clipboard-heart-linear" aria-hidden="true"></span>
          {{ questionType === '1' ? '多选题' : questionType === '2' ? '问答题' : '单选题' }}
        </p>
        <h3>{{ questionType === '3' ? data.title : data.content }}</h3>
      </div>
      <span class="question-status-pill" :class="cardStateClass">
        <span class="iconify" :class="statusIcon" aria-hidden="true"></span>
        {{ statusLabel }}
      </span>
    </header>

    <img v-if="data.imgUrl" class="question-image" :src="data.imgUrl" alt="" />

    <button v-if="showLike && questionType !== '2'" class="question-like-button" type="button" :aria-label="i18n.global.t('system.favorites')" @click="onLike">
      <span class="iconify ic--baseline-star" :class="{ active: data.favoriteFlag === '1' }" aria-hidden="true"></span>
    </button>

    <div v-if="!data.type || data.type == '0'" class="question-answer-area">
      <n-radio-group v-model:value="data.isanswer" :disabled="data.disabled" @update:value="onChecked">
        <n-space vertical class="question-option-list">
          <n-radio v-for="(p, i) in data.options" :key="i" class="question-option" :value="p.title" :label="p.title + '. ' + p.choice" />
        </n-space>
      </n-radio-group>
    </div>

    <div v-if="data.type == '1'" class="question-answer-area">
      <n-checkbox-group v-model:value="multiAnswer" :disabled="data.disabled">
        <n-space vertical class="question-option-list">
          <n-checkbox v-for="(p, i) in data.options" :key="i" class="question-option" :value="p.title" :label="p.title + '. ' + p.choice" />
        </n-space>
      </n-checkbox-group>
      <div class="question-action-row">
        <n-button type="info" :disabled="!multiAnswer.length || data.disabled" @click="changeStatus">{{ i18n.global.t('question.checkMulti') }}</n-button>
      </div>
    </div>

    <div v-if="data.type == '2'" class="question-answer-area">
      <button v-if="showLike && !hideInputer" class="question-like-button text-like" type="button" :aria-label="i18n.global.t('system.favorites')" @click="onLike">
        <span class="iconify ic--baseline-star" :class="{ active: data.favoriteFlag === '1' }" aria-hidden="true"></span>
      </button>
      <n-input
        v-if="!hideInputer"
        v-model:value="textAnswer"
        :disabled="data.disabled"
        type="textarea"
        class="question-textarea"
        @change="onTextChanged"
        :maxlength="500"
        :autosize="{
          minRows: 3,
          maxRows: 5
        }"
      />
      <span v-if="hideInputer" class="ai-answer-label">{{ i18n.global.t('question.aiAnswer') }}</span>
    </div>

    <div v-if="data.type == 3" class="question-answer-area">
      <button class="question-like-button" type="button" :aria-label="i18n.global.t('system.favorites')">
        <span class="iconify ic--baseline-star" aria-hidden="true"></span>
      </button>
      <n-radio-group v-model:value="data.answer">
        <n-space vertical class="question-option-list">
          <n-radio v-for="(p, i) in data.option" :key="i" class="question-option" :value="p.id" :label="String(p.title)" />
        </n-space>
      </n-radio-group>
    </div>

    <transition name="question-feedback">
      <div v-if="feedbackVisible" class="question-feedback-burst" :class="cardStateClass" role="status" aria-live="polite">
        <span class="feedback-particle particle-one"></span>
        <span class="feedback-particle particle-two"></span>
        <span class="feedback-particle particle-three"></span>
        <span class="feedback-icon iconify" :class="feedbackIcon" aria-hidden="true"></span>
        <span class="feedback-copy">{{ feedbackText }}</span>
      </div>
    </transition>

    <section v-if="data.isanswer && showLike" class="question-analysis-panel">
      <div class="answer-summary">
        <div>
          {{ i18n.global.t('question.yourAnswer') }}
          <span :class="isCorrect ? 'answer-correct' : 'answer-wrong'">{{ data.isanswer }}</span>
        </div>
        <div>
          {{ i18n.global.t('question.correctAnswer') }}
          <span class="answer-correct">{{ data.answer }}</span>
        </div>
      </div>
      <div class="analysis-block">
        {{ i18n.global.t('question.analysis') }}:
        <span ref="analysisRef" class="analysis-content" v-html="marked(data.answerExplain || '')"></span>
      </div>
    </section>
  </article>
</template>

<style lang="css" scoped>
.question-card-state {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.94)),
    #ffffff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.86);
  padding: clamp(16px, 2vw, 22px);
  transition: border-color 220ms ease, background-color 220ms ease, box-shadow 220ms ease;
}

.question-card-state + .question-card-state {
  margin-top: 16px;
}

.question-card-state.is-correct {
  border-color: rgba(34, 197, 94, 0.42);
  background:
    linear-gradient(180deg, rgba(240, 253, 244, 0.98), rgba(220, 252, 231, 0.78)),
    #f0fdf4;
  box-shadow: 0 16px 36px rgba(22, 163, 74, 0.12), inset 0 0 0 1px rgba(34, 197, 94, 0.12);
}

.question-card-state.is-wrong {
  border-color: rgba(248, 113, 113, 0.42);
  background:
    linear-gradient(180deg, rgba(254, 242, 242, 0.98), rgba(254, 226, 226, 0.72)),
    #fef2f2;
  box-shadow: 0 16px 36px rgba(220, 38, 38, 0.1), inset 0 0 0 1px rgba(248, 113, 113, 0.14);
}

.question-card-header {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: start;
  gap: 14px;
}

.question-number {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.18);
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(var(--ceres-primary-rgb), 0.14), rgba(8, 145, 178, 0.11));
  color: var(--ceres-primary);
  font-size: 18px;
  font-weight: 900;
}

.question-heading {
  min-width: 0;
}

.question-type-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 6px;
  color: var(--ceres-muted);
  font-size: 12px;
  font-weight: 800;
}

.question-heading h3 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(16px, 1.8vw, 20px);
  font-weight: 850;
  line-height: 1.55;
  letter-spacing: 0;
}

.question-status-pill {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(var(--ceres-primary-rgb), 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #475569;
  padding: 5px 11px;
  font-size: 12px;
  font-weight: 850;
  white-space: nowrap;
}

.question-status-pill.is-correct {
  border-color: rgba(34, 197, 94, 0.28);
  color: #15803d;
}

.question-status-pill.is-wrong {
  border-color: rgba(248, 113, 113, 0.32);
  color: #b91c1c;
}

.question-image {
  display: block;
  width: min(100%, 560px);
  max-height: 320px;
  margin: 18px 0 0 56px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 16px;
  object-fit: contain;
  background: #f8fafc;
}

.question-like-button {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: #94a3b8;
  cursor: pointer;
  font-size: 20px;
  transition: border-color 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.question-like-button:hover,
.question-like-button:focus-visible {
  border-color: rgba(251, 191, 36, 0.46);
  color: #d97706;
  box-shadow: 0 10px 22px rgba(217, 119, 6, 0.12);
  outline: none;
}

.question-like-button .active {
  color: #f59e0b;
}

.question-like-button.text-like {
  top: 14px;
  bottom: auto;
}

.question-answer-area {
  position: relative;
  margin: 18px 0 0 56px;
}

.question-option-list {
  width: 100%;
  padding: 0;
}

.question-option {
  width: 100%;
  min-height: 42px;
  align-items: flex-start;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  padding: 10px 12px;
  transition: border-color 180ms ease, background-color 180ms ease, box-shadow 180ms ease;
}

.question-option:hover {
  border-color: rgba(var(--ceres-primary-rgb), 0.3);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.08);
}

.question-action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.question-textarea {
  width: min(100%, 680px);
}

.ai-answer-label {
  display: inline-flex;
  color: #64748b;
  font-size: 14px;
}

.question-feedback-burst {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 5;
  display: grid;
  min-width: 128px;
  place-items: center;
  gap: 5px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  padding: 12px 16px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.14);
  pointer-events: none;
}

.question-feedback-burst.is-correct {
  color: #15803d;
}

.question-feedback-burst.is-wrong {
  color: #b91c1c;
}

.feedback-icon {
  font-size: 38px;
  animation: feedback-pop 720ms cubic-bezier(0.2, 0.9, 0.2, 1.2) both;
}

.question-feedback-burst.is-wrong .feedback-icon {
  animation: feedback-sorry 720ms ease both;
}

.feedback-copy {
  font-size: 13px;
  font-weight: 900;
}

.feedback-particle {
  position: absolute;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.74;
}

.particle-one {
  top: 10px;
  left: 18px;
  animation: particle-one 900ms ease-out both;
}

.particle-two {
  top: 16px;
  right: 20px;
  animation: particle-two 900ms ease-out both;
}

.particle-three {
  right: 42px;
  bottom: 12px;
  animation: particle-three 900ms ease-out both;
}

.question-feedback-enter-active,
.question-feedback-leave-active {
  transition: opacity 160ms ease, transform 160ms ease;
}

.question-feedback-enter-from,
.question-feedback-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
}

.question-analysis-panel {
  margin: 18px 0 0 56px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  padding: 14px 16px;
}

.answer-summary {
  display: grid;
  gap: 6px;
  color: #334155;
  font-size: 15px;
  font-weight: 800;
}

.answer-correct {
  margin-left: 6px;
  color: #16a34a;
}

.answer-wrong {
  margin-left: 6px;
  color: #dc2626;
}

.analysis-block {
  margin-top: 12px;
  color: #475569;
  font-size: 14px;
  line-height: 1.72;
}

.analysis-content :deep(p) {
  display: inline;
}

:deep(.n-radio__label),
:deep(.n-checkbox__label) {
  color: #1f2937;
  font-size: 15px;
  line-height: 1.55;
}

@keyframes feedback-pop {
  0% {
    transform: translateY(8px) scale(0.5) rotate(-12deg);
    opacity: 0;
  }
  55% {
    transform: translateY(-8px) scale(1.16) rotate(8deg);
    opacity: 1;
  }
  100% {
    transform: translateY(0) scale(1) rotate(0deg);
    opacity: 1;
  }
}

@keyframes feedback-sorry {
  0% {
    transform: translateX(0) scale(0.8);
    opacity: 0;
  }
  25% {
    transform: translateX(-5px) rotate(-5deg);
    opacity: 1;
  }
  50% {
    transform: translateX(5px) rotate(5deg);
  }
  75% {
    transform: translateX(-3px) rotate(-2deg);
  }
  100% {
    transform: translateX(0) rotate(0deg);
    opacity: 1;
  }
}

@keyframes particle-one {
  to {
    transform: translate(-12px, -12px) scale(0.4);
    opacity: 0;
  }
}

@keyframes particle-two {
  to {
    transform: translate(14px, -10px) scale(0.35);
    opacity: 0;
  }
}

@keyframes particle-three {
  to {
    transform: translate(8px, 12px) scale(0.35);
    opacity: 0;
  }
}

@media (max-width: 640px) {
  .question-card-header {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .question-status-pill {
    grid-column: 1 / -1;
    width: fit-content;
    margin-left: 56px;
  }

  .question-image,
  .question-answer-area,
  .question-analysis-panel {
    margin-left: 0;
  }

  .question-feedback-burst {
    top: 12px;
    right: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .question-card-state,
  .question-option,
  .question-like-button,
  .question-feedback-enter-active,
  .question-feedback-leave-active {
    transition: none;
  }

  .feedback-icon,
  .feedback-particle {
    animation: none !important;
  }
}
</style>
