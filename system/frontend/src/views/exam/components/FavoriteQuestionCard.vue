<script lang="ts" setup>
type QuestionOption = {
  title: string | number
  choice?: string
}

type FavoriteQuestion = {
  answer?: string | number | Array<string | number>
  answerExplain?: string
  choiceA?: string
  choiceB?: string
  choiceC?: string
  choiceD?: string
  choiceE?: string
  choiceF?: string
  content?: string
  description?: string
  imgUrl?: string
  options?: QuestionOption[]
  queId?: string | number
  type?: string | number
}

const props = defineProps({
  question: {
    type: Object as PropType<FavoriteQuestion>,
    default: () => ({})
  },
  expanded: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits<{
  toggle: [queId: string | number]
  remove: [queId: string | number]
}>()

const questionId = computed(() => props.question.queId ?? '')
const hasQuestionId = computed(() => questionId.value !== '')
const questionType = computed(() => String(props.question.type ?? '0'))
const typeLabel = computed(() => (questionType.value === '1' ? '多选题' : questionType.value === '2' ? '问答题' : '单选题'))
const normalizedOptions = computed(() => {
  if (props.question.options?.length) {
    return props.question.options.filter(option => String(option.choice ?? '').trim())
  }

  return [
    { title: 'A', choice: props.question.choiceA },
    { title: 'B', choice: props.question.choiceB },
    { title: 'C', choice: props.question.choiceC },
    { title: 'D', choice: props.question.choiceD },
    { title: 'E', choice: props.question.choiceE },
    { title: 'F', choice: props.question.choiceF }
  ].filter(option => String(option.choice ?? '').trim())
})

const answerText = computed(() => {
  const answer = props.question.answer
  return Array.isArray(answer) ? answer.join(', ') : String(answer ?? '')
})

const toggle = () => {
  if (hasQuestionId.value) {
    emit('toggle', questionId.value)
  }
}

const remove = () => {
  if (hasQuestionId.value) {
    emit('remove', questionId.value)
  }
}
</script>

<template>
  <article class="favorite-question-card" :class="{ 'is-expanded': expanded }">
    <button class="favorite-question-main" type="button" :aria-expanded="expanded" @click="toggle">
      <span class="favorite-question-summary">
        <span class="favorite-question-type">{{ typeLabel }}</span>
        <span class="favorite-question-content" :class="{ expanded }">{{ question.content || '-' }}</span>
      </span>

      <span class="favorite-question-detail-shell" :aria-hidden="!expanded">
        <span class="favorite-question-detail">
          <span v-if="question.description" class="favorite-description-tag">
            {{ question.description }}
          </span>

          <img v-if="question.imgUrl" class="favorite-question-image" :src="question.imgUrl" alt="" />

          <span v-if="normalizedOptions.length" class="favorite-option-list">
            <span v-for="option in normalizedOptions" :key="option.title" class="favorite-option-item">
              <strong>{{ option.title }}.</strong>
              <span>{{ option.choice }}</span>
            </span>
          </span>

          <span v-if="answerText" class="favorite-answer-row">
            <strong>{{ $t('question.answer') }}:</strong>
            <span>{{ answerText }}</span>
          </span>

          <span v-if="question.answerExplain" class="favorite-analysis-block">
            <strong>{{ $t('question.analysis') }}:</strong>
            <span>{{ question.answerExplain }}</span>
          </span>
        </span>
      </span>
    </button>

    <button class="favorite-remove-button" type="button" title="取消收藏" aria-label="取消收藏" @click.stop="remove">
      <span class="iconify ic--baseline-star" aria-hidden="true"></span>
    </button>
  </article>
</template>

<style lang="css" scoped>
.favorite-question-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 38px;
  align-items: start;
  gap: 10px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.07);
  padding: 12px;
  transition: border-color 180ms ease, box-shadow 180ms ease, background-color 180ms ease;
}

.favorite-question-card:hover,
.favorite-question-card.is-expanded {
  border-color: rgba(8, 145, 178, 0.34);
  background: #ffffff;
  box-shadow: 0 16px 34px rgba(8, 145, 178, 0.12);
}

.favorite-question-main {
  display: grid;
  min-width: 0;
  gap: 8px;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 0;
  text-align: left;
}

.favorite-question-main:focus-visible {
  border-radius: 10px;
  outline: 3px solid rgba(8, 145, 178, 0.24);
  outline-offset: 3px;
}

.favorite-question-summary {
  display: grid;
  min-width: 0;
  gap: 6px;
}

.favorite-question-type {
  width: fit-content;
  max-width: 100%;
  border-radius: 999px;
  background: #ecfeff;
  color: #0e7490;
  font-size: 12px;
  font-weight: 850;
  line-height: 1.2;
  padding: 4px 8px;
}

.favorite-question-content {
  min-width: 0;
  overflow: hidden;
  color: #334155;
  display: -webkit-box;
  font-size: 14px;
  font-weight: 650;
  line-height: 1.7;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.favorite-question-content.expanded {
  display: block;
  overflow: visible;
}

.favorite-question-detail-shell {
  display: grid;
  grid-template-rows: 0fr;
  overflow: hidden;
  transition: grid-template-rows 200ms ease;
}

.favorite-question-card.is-expanded .favorite-question-detail-shell {
  grid-template-rows: 1fr;
}

.favorite-question-detail {
  display: grid;
  min-height: 0;
  min-width: 0;
  gap: 10px;
  overflow: hidden;
  padding-top: 0;
}

.favorite-question-card.is-expanded .favorite-question-detail {
  padding-top: 4px;
}

.favorite-description-tag {
  display: inline-flex;
  width: fit-content;
  max-width: 100%;
  min-height: 28px;
  height: auto;
  align-items: flex-start;
  border: 1px solid rgba(37, 99, 235, 0.18);
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 760;
  line-height: 1.45;
  overflow-wrap: anywhere;
  padding: 5px 10px;
  white-space: normal;
  word-break: break-word;
}

.favorite-question-image {
  width: 100%;
  max-height: 180px;
  border-radius: 10px;
  object-fit: contain;
  background: #f8fafc;
}

.favorite-option-list {
  display: grid;
  min-width: 0;
  gap: 8px;
}

.favorite-option-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 6px;
  border-radius: 10px;
  background: #f8fafc;
  color: #334155;
  font-size: 13px;
  line-height: 1.55;
  overflow-wrap: anywhere;
  padding: 8px 10px;
}

.favorite-answer-row,
.favorite-analysis-block {
  display: grid;
  min-width: 0;
  gap: 4px;
  border-radius: 10px;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
  overflow-wrap: anywhere;
  padding: 9px 10px;
  white-space: pre-wrap;
}

.favorite-answer-row {
  background: #f0fdf4;
}

.favorite-answer-row strong {
  color: #15803d;
}

.favorite-analysis-block {
  background: #fff7ed;
}

.favorite-analysis-block strong {
  color: #c2410c;
}

.favorite-remove-button {
  display: inline-flex;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(251, 191, 36, 0.28);
  border-radius: 999px;
  background: rgba(255, 251, 235, 0.92);
  color: #d97706;
  cursor: pointer;
  font-size: 23px;
  transition: border-color 180ms ease, box-shadow 180ms ease, color 180ms ease, background-color 180ms ease;
}

.favorite-remove-button:hover {
  border-color: rgba(217, 119, 6, 0.42);
  background: #fffbeb;
  color: #b45309;
  box-shadow: 0 10px 22px rgba(217, 119, 6, 0.14);
}

.favorite-remove-button:focus-visible {
  outline: 3px solid rgba(8, 145, 178, 0.24);
  outline-offset: 2px;
}

@media (max-width: 480px) {
  .favorite-question-card {
    grid-template-columns: minmax(0, 1fr) 36px;
    padding: 11px;
  }

  .favorite-remove-button {
    width: 36px;
    height: 36px;
  }
}
</style>
