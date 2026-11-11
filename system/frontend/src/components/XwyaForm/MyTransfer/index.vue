<template>
  <n-form-item v-show="isShow" :label="label" :style="style"  :rule="rules" :path="path" :key="path+isShow">
    <n-transfer v-model:value="value![path]" style="width: 100%" v-bind="$attrs" />
  </n-form-item>
</template>

<script setup lang="ts">
import type { SelectOption } from "naive-ui";
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

  style: {
    type: Object,
    default: () => ({})
  },
  rules: {
    type: Array,
    default: () => []
  },
  isShow: {
    type: Boolean,
    default: true
  }
})
</script>

<style lang="scss" scoped></style>
