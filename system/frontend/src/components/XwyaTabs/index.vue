<template>
  <n-tabs :type="tabsType" animated class="xwya-tabs" :class="`xwya-tabs--${variant}`" v-bind="$attrs">
    <n-tab-pane v-if="pane" v-for="(item, index) in tablist" :key="index" v-bind="bindItem(item)">
      <component v-if="item.children" :is="initComponent(item.children)"></component>
    </n-tab-pane>
    <n-tab v-if="!pane" v-for="(item, index) in tablist" :key="index" :tab="item.label" :name="item.value" />
    <template v-for="(value, name) in $slots" #[name] :key="name">
      <slot :name="name" :slot="value"></slot>
    </template>
  </n-tabs>
</template>

<script setup lang="ts">
import { computed, type VNode } from 'vue'

const props = defineProps({
  pane: {
    type: Boolean,
    default: false,
  },
  tablist: {
    type: Array,
    default: () => [],
  },
  variant: {
    type: String,
    default: 'line',
  },
})

const tabsType = computed(() => (props.variant === 'segment' ? 'segment' : props.variant === 'card' ? 'card' : 'line'))

const initComponent = (com: VNode | (() => VNode)) => {
  return defineComponent({
    render() {
      if (typeof com === 'function') {
        return com()
      }
      return com
    },
  })
}

const bindItem = (com: any) => {
  return { tab: com.label, name: com.value }
}
</script>

<style lang="scss" scoped>
.xwya-tabs {
  :deep(.n-tabs-tab) {
    min-height: 38px;
    border-radius: 999px;
    font-weight: 700;
    transition: color 180ms ease, background-color 180ms ease;
  }

  :deep(.n-tabs-tab:hover) {
    color: var(--ceres-primary);
  }
}

.xwya-tabs--segment {
  :deep(.n-tabs-nav) {
    padding: 4px;
    border: 1px solid var(--ceres-border);
    border-radius: 999px;
    background: rgba(var(--ceres-card-rgb), 0.84);
  }
}
</style>
