<script lang="ts" setup>
import SubjectBox from "./subjectBox.vue";
const info = ref({
  videoId: 0,
  id: 0,
  v: "",
})
const changeInfo = (obj) => {
  info.value = obj
}
</script>

<template>
  <XwyaLayout
    class="exam-page-layout"
    v-bind="info"
    hide-chat-header
    hide-assistant-description
    :chat-assistant-title="$t('chat.personalizedExercise')"
    :chat-assistant-role-label="$t('chat.virtualEyeTeacher')"
  >
    <template #default="f">
      <SubjectBox v-bind="f" :changeInfo ="changeInfo " />
    </template>
  </XwyaLayout>
</template>

<style lang="scss" scoped>
.exam-page-layout.exam-page-layout {
  width: min(1240px, calc(100% - clamp(32px, 6vw, 72px)));
  height: calc(100% - 12px);
  margin: 10px auto 0;
  padding: 18px;
}

@media (max-width: 900px) {
  .exam-page-layout.exam-page-layout {
    width: calc(100% - 20px);
    height: calc(100% - 10px);
    margin-top: 8px;
    padding: 10px;
  }
}

@media (max-width: 560px) {
  .exam-page-layout.exam-page-layout {
    width: calc(100% - 16px);
    padding: 8px;
  }
}
</style>
