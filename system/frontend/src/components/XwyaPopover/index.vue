<template>
  <n-popover v-bind="$attrs">
    <template  v-for="(value, name) in $slots" #[name] :key="name">
      <slot :name="name" :slot="value"></slot>
    </template>
      {{ text }}

  </n-popover>
</template>

<script setup lang="ts">
const { proxy: Proxys } = getCurrentInstance() as any;

defineProps({
  text: {
    type: String,
    default: ''
  }
})
const getname = (name) => {
  return name === 'default' ? 'trigger':name
}
</script>

<style lang="scss" scoped></style>
