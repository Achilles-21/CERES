<template>
  <n-layout class="ceres-gradient-bg admin-layout-shell" :class="{ 'is-dark': isDark }" has-sider>
    <n-layout-sider
      v-if="pc"
      class="admin-soft-sidebar"
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      :bordered="false"
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="admin-sidebar-inner">
        <div v-if="set.sidebarLogo" class="admin-sidebar-logo">
          <img :src="getImgUrl" alt="Eye Teacher" />
        </div>

        <n-menu
          class="admin-menu"
          :render-icon="renderMenuIcon"
          :default-value="path"
          :theme-overrides="menuThemeOverrides"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="routes"
          @update:value="onUpdareValue"
        />
      </div>
    </n-layout-sider>

    <n-layout class="admin-main-layout">
      <n-layout-header class="admin-topbar" :bordered="false">
        <n-flex :justify="pc ? 'end' : 'space-between'" class="admin-topbar-inner" align="center">
          <button v-if="!pc" class="ceres-icon-button admin-menu-button" type="button" aria-label="Open menu" @click="collapsed = !collapsed">
            <span class="iconify ic--round-menu text-2xl" aria-hidden="true"></span>
          </button>
          <Down />
        </n-flex>
      </n-layout-header>
      <n-layout class="admin-content-scroll" :native-scrollbar="false">
        <main class="ceres-page-shell admin-page-stage">
          <router-view v-slot="{ Component }">
            <transition appear name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </n-layout>
    </n-layout>
    <n-drawer v-if="!pc" v-model:show="collapsed" :width="260" placement="left" class="admin-mobile-drawer">
      <div class="admin-drawer-content">
        <div class="admin-sidebar-logo">
          <img src="/maxLogo.svg" alt="Eye Teacher" />
        </div>
        <n-menu
          class="admin-menu"
          :render-icon="renderMenuIcon"
          :default-value="path"
          :theme-overrides="menuThemeOverrides"
          :collapsed="false"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="routes"
          @update:value="onUpdareValue"
        />
      </div>
    </n-drawer>
  </n-layout>
</template>

<script setup lang="ts">
import Down from './down.vue'
import useLayout from './useLayout'
import set from '@/settings'
const { collapsed, routes, path, onUpdareValue, renderMenuIcon, pc, isDark, menuThemeOverrides } = useLayout()
const getImgUrl = computed(() => {
  return collapsed.value ? '/minLogo.svg' : '/maxLogo.svg'
})
</script>

<style scoped>
.admin-layout-shell {
  width: 100%;
  min-width: 0;
  height: calc(var(--vh, 1vh) * 100);
  overflow: hidden;
  background:
    radial-gradient(circle at 2% 0%, rgba(114, 46, 209, 0.12), transparent 30rem),
    linear-gradient(135deg, #f7f4ff 0%, #f5f7fb 48%, #eef7ff 100%);
}

.admin-layout-shell.is-dark {
  background:
    radial-gradient(circle at 2% 0%, rgba(139, 71, 234, 0.2), transparent 30rem),
    linear-gradient(135deg, #0f1020 0%, #171729 50%, #0e1726 100%);
}

.admin-main-layout {
  min-width: 0;
  height: calc(var(--vh, 1vh) * 100);
  background: transparent;
}

.admin-soft-sidebar {
  height: calc(var(--vh, 1vh) * 100 - 24px);
  margin: 12px 0 12px 12px;
  overflow: hidden;
  border: 1px solid rgba(114, 46, 209, 0.13);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82) !important;
  box-shadow: 0 20px 50px rgba(79, 70, 229, 0.12);
  backdrop-filter: blur(18px);
}

.is-dark .admin-soft-sidebar {
  border-color: rgba(139, 71, 234, 0.28);
  background: rgba(18, 18, 32, 0.82) !important;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.28);
}

.admin-sidebar-inner,
.admin-drawer-content {
  min-width: 0;
  padding: 12px 8px;
}

.admin-sidebar-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 58px;
  margin-bottom: 8px;
}

.admin-sidebar-logo img {
  width: 90%;
  height: 54px;
  object-fit: contain;
}

.admin-topbar {
  height: 76px;
  padding: 12px clamp(12px, 2.5vw, 30px);
  background: transparent !important;
}

.admin-topbar-inner {
  height: 52px;
  min-width: 0;
  padding: 0 4px 0 12px;
  border: 1px solid rgba(114, 46, 209, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.68);
  box-shadow: 0 14px 36px rgba(79, 70, 229, 0.1);
  backdrop-filter: blur(16px);
}

.is-dark .admin-topbar-inner {
  border-color: rgba(139, 71, 234, 0.24);
  background: rgba(18, 18, 32, 0.68);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.22);
}

.admin-menu-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
  cursor: pointer;
  transition:
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.admin-menu-button:hover {
  background: rgba(114, 46, 209, 0.16);
}

.admin-menu-button:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.3);
  outline-offset: 2px;
}

.admin-content-scroll {
  height: calc(var(--vh, 1vh) * 100 - 76px);
  padding: 0 clamp(12px, 2.5vw, 24px) clamp(14px, 2.5vw, 24px);
  background: transparent;
}

.admin-page-stage {
  min-width: 0;
  min-height: 100%;
}

:deep(.admin-menu .n-menu-item-content) {
  margin: 4px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition:
    color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

:deep(.admin-menu .n-menu-item-content::before) {
  border-radius: 10px;
}

:deep(.admin-menu .n-menu-item-content:hover) {
  color: #722ed1;
  background: rgba(114, 46, 209, 0.08);
}

:deep(.admin-menu .n-menu-item-content.n-menu-item-content--selected) {
  color: #5b21b6;
  background: rgba(114, 46, 209, 0.13);
  box-shadow: inset 3px 0 0 #722ed1;
}

:deep(.admin-menu .n-menu-item-content .n-menu-item-content__icon) {
  color: inherit;
}

:deep(.admin-soft-sidebar .n-layout-toggle-button) {
  border-color: rgba(114, 46, 209, 0.18);
  background: #ffffff;
  color: #722ed1;
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.14);
}

:deep(.admin-mobile-drawer .n-drawer-body-content-wrapper) {
  padding: 0;
}

@media (max-width: 991px) {
  .admin-content-scroll {
    padding-inline: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .admin-menu-button,
  :deep(.admin-menu .n-menu-item-content) {
    transition: none;
  }
}
</style>
