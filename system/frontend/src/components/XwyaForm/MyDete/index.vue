<template>
  <n-form-item v-show="isShow" :style="style" :label="label" :path="path" :rule="rules" :key="path+isShow">
  <n-date-picker value-format="yyyy.MM.dd HH:mm:ss" style="width: 100%;" v-model:formatted-value="(value![path] as string)" :type="(nativeType as any)" :clearable="true" v-bind="$attrs" />
</n-form-item>
</template>

<script lang="ts" setup>
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
  nativeType: {
    type: String,
    default: 'date'
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

<style scoped></style>
