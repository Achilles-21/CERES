<template>
  <n-form-item   :label="label" :style="style" :path="path" :rule="rules" v-show="isShow" >
    <n-input  v-model:value="value![path]"  v-bind="$attrs" :type="nativeType" :key="path+isShow" />
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
  nativeType: {
    type: String,
    default: 'text'
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
