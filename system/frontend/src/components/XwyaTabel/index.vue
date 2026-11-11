<template>
  <section :class="['xwya-table-shell', card ? 'ceres-card' : '']">
    <header v-if="title || description || slots.toolbar" class="xwya-table-header">
      <div>
        <h3 v-if="title" class="xwya-table-title">{{ title }}</h3>
        <p v-if="description" class="xwya-table-description">{{ description }}</p>
      </div>
      <div v-if="slots.toolbar" class="xwya-table-toolbar">
        <slot name="toolbar"></slot>
      </div>
    </header>

    <div class="xwya-table-scroll">
      <n-data-table
        class="xwya-data-table"
        :remote="true"
        @update:checked-row-keys="(onSelect as Function)"
        :bordered="false"
        :single-line="false"
        :columns="(columns as any)"
        :data="(data as any)"
        :pagination="(pagination as NaiveUI.PaginationProps)"
        :paginate-single-page="true"
        :loading="loading"
        :striped="false"
        :row-key="setRowKey"
        v-bind="$attrs"
      >
        <template v-for="(value, name) in $slots" #[name] :key="name">
          <slot :name="name" :slot="value"></slot>
        </template>
        <template #empty>
          <div class="xwya-empty-state">
            <span class="iconify solar--database-outline xwya-empty-icon"></span>
            <n-empty :description="$t('system.nodata')" />
          </div>
        </template>
      </n-data-table>
    </div>
  </section>
</template>

<script lang="ts" setup>
import { useSlots } from 'vue'

defineProps({
  columns: {
    type: Array,
    default: () => [],
  },
  data: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  pagination: {
    type: Object,
    default: () => ({}),
  },
  onSelect: {
    type: Function,
    default: () => {},
  },
  title: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  card: {
    type: Boolean,
    default: true,
  },
})

const slots = useSlots()

const setRowKey = (obj: any) => {
  return obj.id
}
</script>

<style lang="scss" scoped>
.xwya-table-shell {
  width: 100%;
  overflow: hidden;
}

.xwya-table-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 18px 12px;
  border-bottom: 1px solid var(--ceres-border);
}

.xwya-table-title {
  margin: 0;
  color: var(--ceres-fg);
  font-size: 18px;
  line-height: 1.2;
}

.xwya-table-description {
  margin-top: 6px;
  color: var(--ceres-muted);
  font-size: 14px;
}

.xwya-table-toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.xwya-table-scroll {
  width: 100%;
  overflow-x: auto;
}

.xwya-data-table {
  min-width: 720px;
}

.xwya-empty-state {
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 26px;
  color: var(--ceres-muted);
}

.xwya-empty-icon {
  color: var(--ceres-primary);
  font-size: 34px;
}

:deep(.n-data-table-th) {
  color: var(--ceres-fg);
}

:deep(.n-data-table-tr) {
  transition: background-color 180ms ease;
}

:deep(.n-data-table-td) {
  border-color: var(--ceres-border);
}

@media (max-width: 768px) {
  .xwya-table-header {
    display: grid;
  }

  .xwya-table-toolbar {
    justify-content: flex-start;
  }
}
</style>
