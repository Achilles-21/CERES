<script lang="ts" setup>
defineProps({
  onEdit: {
    type: Function,
    default: () => {},
  },
  onDelete: {
    type: Function,
    default: () => {},
  },
  teacher: {
    type: Boolean,
    default: false,
  },
  id: {
    type: Number,
    default: 0,
  },
})
</script>

<template>
  <n-space class="bnt-list" :size="8">
    <n-tooltip trigger="hover">
      <template #trigger>
        <XwyaButton @click="onEdit" variant="icon" type="warning" aria-label="edit" icon="iconify gg--pen text-lg" />
      </template>
      {{ $t('pageGlobal.edit') }}
    </n-tooltip>
    <n-tooltip trigger="hover" v-if="teacher">
      <template #trigger>
        <XwyaButton @click="$router.push(`/data/teacher?id=${id}`)" variant="icon" type="primary" aria-label="teacher" icon="iconify ic--baseline-face-4 text-lg" />
      </template>
      {{ $t('pageGlobal.teacher') }}
    </n-tooltip>
    <n-tooltip trigger="hover" v-if="teacher">
      <template #trigger>
        <XwyaButton @click="$router.push(`/data/bank?id=${id}`)" variant="icon" type="info" aria-label="question bank" icon="iconify ic--baseline-error-outline text-lg" />
      </template>
      {{ $t('video.questionBank') }}
    </n-tooltip>
    <XwyaRemind
      :pop-config="{ onPositiveClick: onDelete, negativeText: $t('pageGlobal.cancel'), positiveText: $t('pageGlobal.confirm') }"
      :tooltip-config="{ placement: 'top' }"
      :pop-text="$t('pageGlobal.deleteTips')"
      :tooltip-text="$t('pageGlobal.delete')"
    >
      <XwyaButton type="error" variant="icon" aria-label="delete" icon="iconify gg--trash text-lg" />
    </XwyaRemind>
  </n-space>
</template>

<style lang="scss" scoped>
.bnt-list {
  :deep(.n-button) {
    min-width: 36px;
    width: 36px;
    height: 36px;
    padding: 0;
  }
}
</style>
