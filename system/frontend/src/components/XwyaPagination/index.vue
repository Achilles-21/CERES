<script lang="ts" setup>

const props = defineProps({
  pageNum: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  },
  onUpdate: {
    type: Function,
    default: () => {}
  }
})

</script>

<template>
   <n-pagination v-model:page="page" :page-count="100" />
</template>

<style lang="css" scoped>
</style>
