<template>
  <n-popconfirm v-bind="$attrs">
    <template v-for="(value, name) in $slots" #[name] :key="name">
      <slot :name="name" :slot="value"></slot>
    </template>
    {{ text }}
  </n-popconfirm>
</template>

<script setup lang="ts">
defineProps({
  text: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
