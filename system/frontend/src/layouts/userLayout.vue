<script lang="ts" setup>
import useSystemStore from '@/store/useSystemStore'
import { canUseSlideGen } from '@/utils/slideGenAccess'

import Down from './down.vue'
const systemStore = useSystemStore()
const { push } = useRouter()
const route = useRoute()

const { proxy } = getCurrentInstance() as { proxy: CusProxy }
const navlist = computed(() => [
  { label: proxy.$t('home.home'), key: '/home' },
  { label: proxy.$t('home.goToClass'), key: '/course', group: 'class' },
  ...(canUseSlideGen(systemStore.userInfo) ? [{ label: proxy.$t('home.slideGen'), key: '/slideGen', group: 'class' }] : []),
  { label: proxy.$t('home.goPractice'), key: '/exam', group: 'exercise' },
  { label: proxy.$t('home.PreviousNotesAndReports'), key: '/pastlist', group: 'note' },
  { label: proxy.$t('home.discussion'), key: '/forum', group: 'discuss' },
])
const dropdownNavOptions = computed(() => navlist.value.map(({ label, key }) => ({ label, key })))
const activeNavKey = computed(() => {
  const exactItem = navlist.value.find((item) => item.key === route.path)
  if (exactItem) return exactItem.key

  const group = route.meta?.group as string | undefined
  const groupItem = navlist.value.find((item) => item.group === group)
  return groupItem?.key || '/home'
})
const currentTitle = computed(() => navlist.value.find((item) => item.key === activeNavKey.value)?.label || proxy.$t('home.studySystem'))
const showCeresIntro = ref(false)
const currentLang = computed(() => String(systemStore.lang || '').toLowerCase())
const ceresIntroCopy = computed(() => {
  if (currentLang.value.includes('malay') || currentLang.value.startsWith('ms')) {
    return {
      title: 'Tentang CERES',
      body: 'CERES ialah platform pengajaran pintar AI yang diperibadikan untuk pelajar perubatan. Ia menggabungkan kursus, latihan, analisis kes, nota dan laporan pembelajaran dalam satu ruang kerja fokus, lalu membantu membina laluan pembelajaran yang sesuai dengan kemajuan anda.',
      close: 'Faham'
    }
  }

  if (currentLang.value.includes('hindi') || currentLang.value.startsWith('hi')) {
    return {
      title: 'CERES के बारे में',
      body: 'CERES मेडिकल छात्रों के लिए एक वैयक्तिकृत AI शिक्षण मंच है। यह कोर्स, अभ्यास, केस विश्लेषण, नोट्स और सीखने की रिपोर्ट को एक केंद्रित कार्यक्षेत्र में जोड़ता है और आपकी प्रगति के अनुसार बेहतर सीखने का मार्ग बनाने में मदद करता है।',
      close: 'समझ गया'
    }
  }

  if (currentLang.value.startsWith('en')) {
    return {
      title: 'About CERES',
      body: 'CERES is a personalized AI teaching platform for medical students. It brings courses, practice, case analysis, notes, and learning reports into one focused workspace, then helps shape a study path that fits your progress and learning needs.',
      close: 'Got it'
    }
  }

  return {
    title: '关于 CERES',
    body: 'CERES 是一个面向医学生的个性化 AI 智能教学平台。它会围绕你的课程进度、练习表现、病例分析和学习记录，组织更适合你的学习路径，帮助你把医学知识学得更系统、更高效。',
    close: '我知道了'
  }
})
const openCeresIntro = () => {
  showCeresIntro.value = true
}
const closeCeresIntro = () => {
  showCeresIntro.value = false
}
const handleSelect = (key: string) => {
  if (route.path !== key) {
    push(key)
  }
}
</script>

<template>
  <div class="ceres-gradient-bg user-layout-shell" :class="{ 'is-dark': systemStore.theme }">
    <header class="user-header-wrap">
      <div class="ceres-glass user-header">
        <button class="brand-button" type="button" :aria-label="ceresIntroCopy.title" aria-haspopup="dialog" @click="openCeresIntro">
          <img src="/maxLogo.svg" class="brand-logo" alt="Eye Teacher" />
        </button>

        <nav class="user-main-nav" :aria-label="$t('home.studySystem')">
          <button
            v-for="item in navlist"
            :key="item.key"
            class="user-nav-item"
            :class="{ 'is-active': item.key === activeNavKey }"
            type="button"
            @click="handleSelect(item.key)"
          >
            {{ item.label }}
          </button>
        </nav>

        <n-dropdown trigger="click" :options="dropdownNavOptions" @select="handleSelect">
          <button class="user-route-trigger" type="button">
            <span>{{ currentTitle }}</span>
            <span class="iconify ic--round-keyboard-arrow-down text-xl" aria-hidden="true"></span>
          </button>
        </n-dropdown>

        <div class="user-actions">
          <Down />
        </div>
      </div>
    </header>

    <main class="user-content-shell">
      <router-view v-slot="{ Component }">
        <transition appear name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <Teleport to="body">
      <transition name="ceres-intro-fade">
        <div v-if="showCeresIntro" class="ceres-intro-overlay" :class="{ 'is-dark': systemStore.theme }" role="presentation" @click.self="closeCeresIntro">
          <section class="ceres-intro-dialog ceres-glass" role="dialog" aria-modal="true" aria-labelledby="ceres-intro-title">
            <button class="ceres-intro-close ceres-icon-button" type="button" :aria-label="ceresIntroCopy.close" @click="closeCeresIntro">
              <span class="iconify solar--close-circle-linear" aria-hidden="true"></span>
            </button>
            <div class="ceres-intro-mark" aria-hidden="true">
              <img src="/minLogo.svg" class="ceres-intro-logo" alt="" />
            </div>
            <h2 id="ceres-intro-title">{{ ceresIntroCopy.title }}</h2>
            <p>{{ ceresIntroCopy.body }}</p>
            <button class="ceres-intro-action ceres-pill" type="button" @click="closeCeresIntro">
              {{ ceresIntroCopy.close }}
            </button>
          </section>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<style lang="css" scoped>
.user-layout-shell {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  height: calc(var(--vh, 1vh) * 100);
  min-height: calc(var(--vh, 1vh) * 100);
  overflow-x: hidden;
  background:
    radial-gradient(circle at 12% 0%, rgba(139, 71, 234, 0.18), transparent 32rem),
    radial-gradient(circle at 90% 10%, rgba(6, 182, 212, 0.14), transparent 28rem),
    linear-gradient(135deg, #f8f5ff 0%, #eef2ff 48%, #f8fbff 100%);
}

.user-layout-shell.is-dark {
  background:
    radial-gradient(circle at 12% 0%, rgba(139, 71, 234, 0.22), transparent 32rem),
    radial-gradient(circle at 90% 10%, rgba(6, 182, 212, 0.14), transparent 28rem),
    linear-gradient(135deg, #0f1020 0%, #17132a 52%, #0d1524 100%);
}

.user-header-wrap {
  position: relative;
  z-index: 30;
  flex-shrink: 0;
  padding: clamp(10px, 2vw, 18px) clamp(12px, 3vw, 32px) 8px;
}

.user-header {
  display: grid;
  grid-template-columns: minmax(128px, 188px) 1fr auto;
  align-items: center;
  gap: clamp(10px, 2vw, 20px);
  width: min(1180px, 100%);
  min-height: 64px;
  margin: 0 auto;
  padding: 8px 10px 8px 14px;
  border: 1px solid rgba(114, 46, 209, 0.16);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 45px rgba(79, 70, 229, 0.13);
  backdrop-filter: blur(18px);
}

.is-dark .user-header {
  border-color: rgba(139, 71, 234, 0.32);
  background: rgba(18, 18, 32, 0.72);
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.28);
}

.brand-button,
.user-nav-item,
.user-route-trigger {
  border: 0;
  cursor: pointer;
  transition:
    color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease,
    transform 180ms ease;
}

.brand-button {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  min-width: 0;
  height: 48px;
  padding: 4px 8px;
  border-radius: 18px;
  background: transparent;
}

.brand-button:hover {
  background: rgba(114, 46, 209, 0.08);
}

.brand-button:focus-visible,
.user-nav-item:focus-visible,
.user-route-trigger:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.3);
  outline-offset: 3px;
}

.brand-logo {
  width: 100%;
  max-width: 176px;
  height: 100%;
  object-fit: contain;
}

.user-main-nav {
  display: none;
  min-width: 0;
  max-width: 100%;
  flex-wrap: wrap;
  align-items: center;
  align-content: center;
  justify-content: flex-start;
  gap: 6px;
  overflow: visible;
  padding: 2px;
}

@supports (justify-content: safe center) {
  .user-main-nav {
    justify-content: safe center;
  }
}

.user-nav-item {
  flex: 0 0 auto;
  min-width: max-content;
  height: 40px;
  padding: 0 13px;
  border-radius: 999px;
  background: transparent;
  color: #312253;
  font-size: 13px;
  font-weight: 700;
  line-height: 40px;
  white-space: nowrap;
}

.user-nav-item:hover {
  background: rgba(114, 46, 209, 0.08);
  color: #722ed1;
}

.user-nav-item.is-active {
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.16), rgba(6, 182, 212, 0.1));
  color: #5b21b6;
  box-shadow: inset 0 0 0 1px rgba(114, 46, 209, 0.22);
}

.is-dark .user-nav-item {
  color: #ede9fe;
}

.is-dark .user-nav-item:hover,
.is-dark .user-nav-item.is-active {
  color: #ffffff;
  background: rgba(139, 71, 234, 0.28);
}

.user-route-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  max-width: 100%;
  height: 42px;
  padding: 0 12px 0 16px;
  border-radius: 999px;
  background: rgba(114, 46, 209, 0.09);
  color: #3b1b67;
  font-weight: 800;
}

.user-route-trigger span:first-child {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-route-trigger:hover {
  background: rgba(114, 46, 209, 0.15);
}

.is-dark .user-route-trigger {
  background: rgba(139, 71, 234, 0.26);
  color: #f4f0ff;
}

.user-actions {
  min-width: 0;
  justify-self: end;
}

.user-content-shell {
  position: relative;
  flex: 1;
  width: 100%;
  height: 0;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.user-content-shell :deep(> *) {
  width: 100%;
  height: 100%;
  min-height: 0;
}

.ceres-intro-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(15, 23, 42, 0.32);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.ceres-intro-dialog {
  position: relative;
  width: min(92vw, 520px);
  padding: clamp(22px, 4vw, 30px);
  border-color: rgba(255, 255, 255, 0.72);
  background:
    linear-gradient(135deg, rgba(114, 46, 209, 0.1), rgba(8, 145, 178, 0.08)),
    rgba(255, 255, 255, 0.92);
  color: #0f172a;
  box-shadow: 0 28px 70px rgba(48, 28, 96, 0.22);
}

.ceres-intro-overlay.is-dark .ceres-intro-dialog {
  border-color: rgba(255, 255, 255, 0.14);
  background:
    linear-gradient(135deg, rgba(139, 71, 234, 0.2), rgba(34, 211, 238, 0.1)),
    rgba(23, 24, 42, 0.94);
  color: #f8fafc;
}

.ceres-intro-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 36px;
  height: 36px;
  font-size: 20px;
}

.ceres-intro-mark {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.16), rgba(8, 145, 178, 0.12));
  color: #722ed1;
  font-size: 25px;
}

.ceres-intro-logo {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.ceres-intro-overlay.is-dark .ceres-intro-mark {
  color: #c4b5fd;
  background: linear-gradient(135deg, rgba(139, 71, 234, 0.28), rgba(34, 211, 238, 0.16));
}

.ceres-intro-dialog h2 {
  margin: 0;
  padding-right: 36px;
  color: inherit;
  font-size: clamp(22px, 3vw, 28px);
  font-weight: 800;
  line-height: 1.18;
}

.ceres-intro-dialog p {
  margin: 14px 0 0;
  color: #475569;
  font-size: 15px;
  line-height: 1.75;
}

.ceres-intro-overlay.is-dark .ceres-intro-dialog p {
  color: #cbd5e1;
}

.ceres-intro-action {
  min-width: 112px;
  margin-top: 22px;
  padding: 0 20px;
  border-color: rgba(114, 46, 209, 0.24);
  background: rgba(114, 46, 209, 0.1);
  color: #5b21b6;
}

.ceres-intro-overlay.is-dark .ceres-intro-action {
  border-color: rgba(196, 181, 253, 0.28);
  background: rgba(139, 71, 234, 0.24);
  color: #ede9fe;
}

.ceres-intro-fade-enter-active,
.ceres-intro-fade-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.ceres-intro-fade-enter-from,
.ceres-intro-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (min-width: 1180px) {
  .user-header {
    grid-template-columns: minmax(148px, 196px) minmax(0, 1fr) auto;
  }

  .user-main-nav {
    display: flex;
  }

  .user-route-trigger {
    display: none;
  }
}

@media (max-width: 640px) {
  .user-header {
    grid-template-columns: minmax(92px, 118px) minmax(0, 1fr) auto;
    gap: 8px;
    min-height: 58px;
    padding: 7px;
    border-radius: 20px;
  }

  .brand-button {
    height: 42px;
    padding: 4px;
  }

  .user-route-trigger {
    height: 38px;
    padding: 0 9px 0 12px;
    font-size: 13px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .brand-button,
  .user-nav-item,
  .user-route-trigger,
  .ceres-intro-fade-enter-active,
  .ceres-intro-fade-leave-active {
    transition: none;
  }
}
</style>
