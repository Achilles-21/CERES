<script lang="ts" setup>
import SubjectBox from './subjectBox.vue'

const { query } = useRoute()
const info = ref({
  videoId: 0,
  id: 0,
  v: ''
})
const changeInfo = (obj: { videoId: string | number; id: string | number; v: string }) => {
  info.value = {
    videoId: Number(obj.videoId),
    id: Number(obj.id),
    v: obj.v
  }
}
</script>

<template>
  <XwyaLayout v-bind="info" class="practice-page-layout">
    <template #default="f">
      <SubjectBox v-bind="f" :cateId="query.cateId" :changeInfo="changeInfo" />
    </template>
  </XwyaLayout>
</template>

<style lang="scss" scoped>
.practice-page-layout {
  height: 100%;
  min-height: 0;
}
</style>
