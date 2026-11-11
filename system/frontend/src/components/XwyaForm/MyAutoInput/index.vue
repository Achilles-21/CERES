<template>
  <n-form-item v-show="isShow" :key="path+isShow"  :style="style" :label="label" :path="path" :rule="rules" v-bind="$attrs">
    <n-auto-complete v-model:value="value![path]" :input-props="{ autocomplete: 'disabled' }"  clearable v-bind="$attrs" />
  </n-form-item>
</template>

<script setup lang="ts">
defineProps({
  value: {
    type: Object,
    default: void 0,
  },
  label: {
    type: String,
    default: ''
  },
  path: {
    type: String,
    default: ''
  },
  rules: {
    type: Array,
    default: () => []
  },
  style: {
    type: Object,
    default: () => ({})
  },
  isShow: {
    type: Boolean,
    default: true
  }
})





</script>

<style lang="css" scoped></style>
