<template>
  <n-form-item v-show="isShow" :style="style" :label="label" :path="path" :rule="rules" :key="path+isShow">
    <n-cascader v-model:value="value![path]" expand-trigger="click" check-strategy="child" :show-path="true"
      :filterable="true" v-bind="$attrs" />
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

<style lang="scss" scoped></style>
