<template>
  <n-config-provider :locale="zhCN" :theme="systemStore.theme" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <div class="ceres-app" :class="{ 'ceres-dark': Boolean(systemStore.theme) }">
      <router-view></router-view>
      <Offline></Offline>
    </div>
  </n-config-provider>
</template>

<script setup lang="ts">
import { zhCN, dateZhCN, type GlobalThemeOverrides } from 'naive-ui'
import useSystemStore from '@/store/useSystemStore'
import useScreen from '@/hooks/useScreen'
import Offline from '@/components/Offline/index.vue'

const systemStore = useSystemStore()
useScreen()

const themeOverrides: GlobalThemeOverrides = {
  common: {
    fontFamily: "'AppLatin', 'AppCJK', system-ui, -apple-system, sans-serif",
    fontFamilyMono: "'AppLatin', 'AppCJK', ui-monospace, monospace",
    fontWeightStrong: '700',
    primaryColor: '#722ED1',
    primaryColorHover: '#8B47EA',
    primaryColorPressed: '#5B21B6',
    primaryColorSuppl: '#8B47EA',
    infoColor: '#4F46E5',
    successColor: '#059669',
    warningColor: '#D97706',
    errorColor: '#DC2626',
    borderRadius: '14px',
    borderRadiusSmall: '10px',
    boxShadow2: '0 18px 50px rgba(48, 28, 96, 0.12)',
  },
  Button: {
    borderRadiusMedium: '999px',
    borderRadiusLarge: '999px',
    fontWeight: '700',
    textColorPrimary: '#FFFFFF',
    textColorHoverPrimary: '#FFFFFF',
  },
  Card: {
    borderRadius: '14px',
    paddingMedium: '22px',
  },
  DataTable: {
    borderRadius: '14px',
    thFontWeight: '700',
    thColor: 'rgba(114, 46, 209, 0.07)',
    tdColorHover: 'rgba(114, 46, 209, 0.05)',
  },
  Input: {
    borderRadius: '12px',
    borderHover: '1px solid #8B47EA',
    borderFocus: '1px solid #722ED1',
    boxShadowFocus: '0 0 0 3px rgba(114, 46, 209, 0.16)',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '12px',
        borderHover: '1px solid #8B47EA',
        borderActive: '1px solid #722ED1',
        boxShadowActive: '0 0 0 3px rgba(114, 46, 209, 0.16)',
      },
    },
  },
}

onMounted(() => {
  systemStore.getLangOption()
})
</script>
