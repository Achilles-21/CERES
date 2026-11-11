<template>
  <n-popconfirm v-bind="popConfig">
    <template #icon v-if="icon">
      <n-icon :size="iconSize">
        <span :class="icon"></span>
      </n-icon>
    </template>
    <template #trigger>
      <n-tooltip trigger="hover" v-bind="tooltipConfig">
        <template #trigger>
            <slot ></slot>
        </template>
        <span>{{ tooltipText }}</span>
      </n-tooltip>
    </template>
   {{ popText }}
  </n-popconfirm>
</template>

<script setup lang="ts">
defineProps({
  tooltipConfig: {
    type: Object,
    default: () => { }
  },
  popConfig: {
    type: Object,
    default: () => { }
  },
  popText: {
    type: String,
    default:""
  },
  tooltipText: {
    type: String,
    default:""
  },
  icon: {
    type: String,
    default:""
  },
  iconSize: {
    type: [Number, String],
    default:24
  }
})
</script>

<style lang="scss" scoped></style>
