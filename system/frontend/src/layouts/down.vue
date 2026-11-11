<template>
  <div class="ceres-glass ceres-pill layout-control-pill" :class="{ 'is-dark': systemStore.theme }">
    <n-dropdown :options="langOptions" trigger="click" @select="handleLangChange">
      <button class="ceres-icon-button layout-control-button" type="button" :aria-label="languageTitle" :title="languageTitle">
        <span class="iconify ic--round-language text-xl" aria-hidden="true"></span>
      </button>
    </n-dropdown>

    <n-dropdown :options="options" trigger="click" @select="handleSelect">
      <button class="layout-user-button" type="button" :aria-label="displayName">
        <n-avatar v-if="settings.avatar && systemStore.prot === '0'" round size="small" :src="userInfo.avatar" />
        <span v-else class="iconify ic--round-account-circle text-xl" aria-hidden="true"></span>
        <span class="layout-user-name">{{ displayName }}</span>
        <span class="iconify ic--round-keyboard-arrow-down layout-user-chevron text-lg" aria-hidden="true"></span>
      </button>
    </n-dropdown>
  </div>
</template>

<script setup lang="ts">
import useSystemStore from '@/store/useSystemStore'
import settings from '@/settings'
import Avatar from '@/components/Avatar'
import TeacherAvatar from '@/components/TeacherAvatar'
const systemStore = useSystemStore()
const { proxy } = getCurrentInstance() as { proxy: CusProxy }
type LanguageOption = { label: string; value: string }
type UserInfo = Record<string, any>
const fallbackLangOptions = [
  { label: '简体中文', key: 'zhcn' },
  { label: 'Bahasa Melayu', key: 'malay' },
  { label: 'हिन्दी', key: 'hindi' },
  { label: 'English', key: 'enus' },
]
const userInfo = computed(() => systemStore.userInfo as UserInfo)
const langOptions = computed(() => {
  const options = systemStore.languageOption as LanguageOption[]
  if (!options?.length) return fallbackLangOptions
  return options.map((item) => ({ label: item.label, key: item.value }))
})
const displayName = computed(() => userInfo.value.nickName || userInfo.value.userName || proxy.$t('login.login'))
const languageTitle = computed(() => proxy.$t('login.langTips'))
const options = computed(() => (systemStore.prot === '0' ? [
  {
    label: proxy.$t('system.setAvatar'),
    key: 'setAvatar',
  },
  {
    label: proxy.$t('system.teacherAvatar'),
    key: 'teacherAvatar',
  },
  {
    label: proxy.$t('adminDown.logOut'),
    key: 'logout',
  },
] : [
  {
    label: proxy.$t('adminDown.logOut'),
    key: 'logout',
  },
]))

const handleSelect = (key: string | number) => {
  switch (key) {
    case 'logout':
      systemStore.resetStore()
      localStorage.removeItem('token')
      window.location.reload()
      break
    case 'setAvatar':
      const m = window.$modal.create({
        title: proxy.$t('system.setAvatar'),
        preset: 'card',
        maskClosable: false,
        style: {
          width: '520px',
        },
        content: () => h(Avatar, { close: () => m.destroy(), initInfo: () => systemStore.getUserInfo(), avatar: userInfo.value.avatar }),
      })
      break
    case 'teacherAvatar':
      const mm = window.$modal.create({
        title: proxy.$t('system.teacherAvatar'),
        preset: 'card',
        maskClosable: false,
        style: {
          width: '540px',
        },
        content: () => h(TeacherAvatar, { close: () => mm.destroy(), initInfo: () => systemStore.getUserInfo(), teacherPicture: userInfo.value.teacherPicture }),
      })
      break
    default:
      console.log('default')
      break
  }
}
const handleLangChange = (key: string | number) => {
  systemStore.changeLang(key)
}
</script>

<style lang="css" scoped>
.layout-control-pill {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  gap: 4px;
  padding: 4px;
  border: 1px solid rgba(114, 46, 209, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.11);
  backdrop-filter: blur(16px);
}

.layout-control-pill.is-dark {
  border-color: rgba(139, 71, 234, 0.3);
  background: rgba(24, 24, 38, 0.72);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
}

.layout-control-button,
.layout-user-button {
  border: 0;
  cursor: pointer;
  color: #3b1b67;
  transition:
    color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.layout-control-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: transparent;
}

.layout-user-button {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  max-width: 220px;
  height: 36px;
  gap: 8px;
  padding: 0 10px 0 6px;
  border-radius: 999px;
  background: transparent;
  font-weight: 700;
}

.layout-control-button:hover,
.layout-user-button:hover {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.layout-control-button:focus-visible,
.layout-user-button:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.3);
  outline-offset: 2px;
}

.layout-control-pill.is-dark .layout-control-button,
.layout-control-pill.is-dark .layout-user-button {
  color: #f4f0ff;
}

.layout-control-pill.is-dark .layout-control-button:hover,
.layout-control-pill.is-dark .layout-user-button:hover {
  background: rgba(139, 71, 234, 0.26);
  color: #ffffff;
}

.layout-user-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .layout-control-pill {
    gap: 2px;
    padding: 3px;
  }

  .layout-control-button {
    width: 34px;
    height: 34px;
  }

  .layout-user-button {
    width: 34px;
    height: 34px;
    justify-content: center;
    padding: 0;
  }

  .layout-user-name,
  .layout-user-chevron {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .layout-control-button,
  .layout-user-button {
    transition: none;
  }
}
</style>
