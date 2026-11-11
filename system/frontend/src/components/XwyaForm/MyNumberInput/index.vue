<template>
  <n-form-item v-show="isShow" :style="style" :label="label" :path="path" :rule="rules" :key="path + isShow">
    <n-input-number style="width: 100%;" button-placement="both" v-model:value="value![path]" v-bind="$attrs" />
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
  placeholder: {
    type: String,
    default: ''
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
