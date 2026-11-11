<template>
  <n-button :class="buttonClasses" :aria-label="ariaLabel || text || undefined" v-bind="$attrs">
    <template #icon v-if="icon">
      <n-icon :size="iconSize">
        <span :class="icon"></span>
      </n-icon>
    </template>
    <span v-if="text">{{ text }}</span>
    <template v-if="!text" #default>
      <slot></slot>
    </template>
  </n-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'icon' | ''

const props = withDefaults(defineProps<{
  text?: string
  icon?: string
  iconSize?: number | string
  variant?: ButtonVariant
  ariaLabel?: string
}>(), {
  text: '',
  icon: '',
  iconSize: 24,
  variant: '',
  ariaLabel: '',
})

const buttonClasses = computed(() => [
  'xwya-button',
  props.variant ? `xwya-button--${props.variant}` : '',
  !props.text && props.icon ? 'xwya-button--icon-only' : '',
])
</script>

<style lang="scss" scoped>
.xwya-button {
  font-weight: 700;
  transition: color 180ms ease, background-color 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.xwya-button:hover {
  box-shadow: 0 8px 22px rgba(var(--ceres-primary-rgb), 0.12);
}

.xwya-button--primary {
  --n-color: var(--ceres-primary);
  --n-color-hover: var(--ceres-primary-hover);
  --n-color-pressed: #5b21b6;
  --n-color-focus: var(--ceres-primary-hover);
  --n-border: 1px solid transparent;
  --n-border-hover: 1px solid transparent;
  --n-border-pressed: 1px solid transparent;
  --n-border-focus: 1px solid transparent;
  --n-text-color: #fff;
  --n-text-color-hover: #fff;
  --n-text-color-pressed: #fff;
  --n-text-color-focus: #fff;
}

.xwya-button--secondary {
  --n-color: var(--ceres-primary-soft);
  --n-color-hover: rgba(var(--ceres-primary-rgb), 0.18);
  --n-color-pressed: rgba(var(--ceres-primary-rgb), 0.22);
  --n-border: 1px solid rgba(var(--ceres-primary-rgb), 0.18);
  --n-border-hover: 1px solid rgba(var(--ceres-primary-rgb), 0.32);
  --n-text-color: var(--ceres-primary);
  --n-text-color-hover: var(--ceres-primary);
}

.xwya-button--outline {
  --n-color: transparent;
  --n-color-hover: var(--ceres-primary-soft);
  --n-border: 1px solid var(--ceres-border);
  --n-border-hover: 1px solid rgba(var(--ceres-primary-rgb), 0.5);
  --n-text-color: var(--ceres-fg);
  --n-text-color-hover: var(--ceres-primary);
}

.xwya-button--ghost {
  --n-color: transparent;
  --n-color-hover: var(--ceres-primary-soft);
  --n-border: 1px solid transparent;
  --n-border-hover: 1px solid transparent;
  --n-text-color: var(--ceres-muted);
  --n-text-color-hover: var(--ceres-primary);
}

.xwya-button--danger {
  --n-color: var(--ceres-danger);
  --n-color-hover: #ef4444;
  --n-color-pressed: #b91c1c;
  --n-border: 1px solid transparent;
  --n-border-hover: 1px solid transparent;
  --n-text-color: #fff;
  --n-text-color-hover: #fff;
}

.xwya-button--icon,
.xwya-button--icon-only {
  min-width: 40px;
  width: 40px;
  padding-inline: 0;
}
</style>
