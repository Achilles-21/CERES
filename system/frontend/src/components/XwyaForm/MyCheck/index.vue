<template>
  <n-form-item v-show="isShow" :label="label" :style="style" :path="path" :rule="rules">
    <n-checkbox-group v-model:value="value![path]" :key="path + isShow">
      <n-checkbox v-for="(p, i) in options" :key="i" :value="(p as any).value" :label="(p as any).label">
      </n-checkbox>
    </n-checkbox-group>
  </n-form-item>
</template>

<script setup lang="ts">
const props = defineProps({
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
  options: {
    type: Array,
    default: () => []
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
  },

})
</script>

<style scoped></style>
