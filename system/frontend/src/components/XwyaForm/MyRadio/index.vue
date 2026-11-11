<template>
<n-form-item v-show="isShow"   :style="style" :label="label" :path="path" :rule="rules"  >
      <n-radio-group v-model:value="value![path]" :key="path+isShow" >
          <n-radio v-for="(p,i) in options" :key="i" :value="(p as any).value" :label="(p as any).label">
          </n-radio>
      </n-radio-group>
    </n-form-item>
</template>

<script setup  lang="ts">
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

<style  scoped></style>
