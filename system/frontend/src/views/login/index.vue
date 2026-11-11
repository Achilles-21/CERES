<template>
  <section class="ceres-gradient-bg login-page" :class="{ 'is-dark': systemStore.theme }">
    <div class="login-texture" aria-hidden="true"></div>
    <div class="login-shell">
      <div class="ceres-card ceres-glass login-panel">
        <div class="login-panel-top">
          <div class="login-brand">
            <div class="login-logo-mark">
              <img src="/minLogo.svg" alt="CERES" />
            </div>
            <div class="login-title-block">
              <h1>CERES</h1>
            </div>
          </div>
          <n-select
            v-model:value="info.language"
            class="login-language-select"
            size="small"
            :placeholder="$t('login.langTips')"
            :options="languageOptions"
            @update:value="onLangChange"
          />
        </div>

        <p class="login-subtitle">AI Teaching Platform</p>

        <div class="login-segmented" role="tablist" :aria-label="$t('login.login')">
          <button
            v-for="item in tabList"
            :key="item.value"
            class="login-segment"
            :class="{ 'is-active': systemStore.prot === item.value }"
            type="button"
            role="tab"
            :aria-selected="systemStore.prot === item.value"
            @click="onTabsChange(item.value)"
          >
            {{ item.label }}
          </button>
        </div>

        <n-form class="login-form" :model="info" label-placement="top" size="large">
          <n-form-item path="username" :label="$t('login.account')">
            <n-input v-model:value="info.username" clearable :placeholder="$t('login.accountTips')" @keyup.enter="login">
              <template #prefix>
                <SvgIcon name="user" className="!text-lg"></SvgIcon>
              </template>
            </n-input>
          </n-form-item>
          <n-form-item path="password" :label="$t('login.password')">
            <n-input v-model:value="info.password" :type="type" :placeholder="$t('login.passwordTips')" @keyup.enter="login">
              <template #prefix>
                <SvgIcon name="password" className="!text-lg"></SvgIcon>
              </template>
              <template #suffix>
                <button class="password-toggle" type="button" :title="passwordTitle" :aria-label="passwordTitle" @click="togglePassword">
                  <SvgIcon v-if="type === 'password'" name="eye" className="!text-lg"></SvgIcon>
                  <SvgIcon v-else name="eye-open" className="!text-lg"></SvgIcon>
                </button>
              </template>
            </n-input>
          </n-form-item>
          <n-form-item class="login-submit-row">
            <XwyaButton
              class="login-submit"
              type="primary"
              size="large"
              :loading="loginLoading"
              :disabled="loginLoading"
              :text="$t('login.login')"
              block
              @click="login"
            />
          </n-form-item>
        </n-form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import SvgIcon from './../../components/SvgIcon/index.vue'
import useSystemStore from '@/store/useSystemStore'
const { proxy } = getCurrentInstance() as { proxy: CusProxy }
type LanguageOption = { label: string; value: string }

const systemStore = useSystemStore()
const { push } = useRouter()
const { query } = useRoute()
const info = reactive({
  type: systemStore.prot,
  username: '',
  password: '',
  language: systemStore.lang,
})
const type = ref('password')
const loginLoading = ref(false)
const fallbackLangOptions = [
  { label: '简体中文', value: 'zhcn' },
  { label: 'Bahasa Melayu', value: 'malay' },
  { label: 'हिन्दी', value: 'hindi' },
  { label: 'English', value: 'enus' },
]
const languageOptions = computed<LanguageOption[]>(() => {
  const options = systemStore.languageOption as LanguageOption[]
  return options?.length ? options : fallbackLangOptions
})
const tabList = computed(() => [
  { label: proxy.$t('login.user'), value: '0' },
  { label: proxy.$t('login.admin'), value: '1' },
])
const passwordTitle = computed(() => (type.value === 'password' ? '显示密码' : '隐藏密码'))
const togglePassword = () => {
  type.value = type.value === 'password' ? 'text' : 'password'
}
const onTabsChange = (value: string) => {
  info.type = value
  systemStore.prot = value
}
const login = async () => {
  if (loginLoading.value) return

  loginLoading.value = true
  const m = window.$msg.loading(proxy.$t('system.loginload'), { duration: 0 })
  try {
    const res = await proxy.$api.system.login(info)

    if (res.code === 200) {
      localStorage.setItem('token', res.token)
      const redirectPath = typeof query.redirect === 'string' ? query.redirect : '/'
      push({ path: redirectPath })
    }
  } finally {
    m.destroy()
    loginLoading.value = false
  }
}

const onLangChange = (v: string) => {
  info.language = v
  systemStore.changeLang(v)
}
</script>

<style lang="css" scoped>
.login-page {
  position: relative;
  display: flex;
  width: 100%;
  min-width: 0;
  min-height: calc(var(--vh, 1vh) * 100);
  overflow-x: hidden;
  overflow-y: auto;
  padding: clamp(18px, 4vw, 48px);
  background:
    radial-gradient(circle at 18% 12%, rgba(139, 71, 234, 0.25), transparent 30rem),
    radial-gradient(circle at 84% 18%, rgba(6, 182, 212, 0.18), transparent 28rem),
    radial-gradient(circle at 72% 86%, rgba(16, 185, 129, 0.14), transparent 24rem),
    linear-gradient(135deg, #f8f5ff 0%, #eef2ff 52%, #f7fffb 100%);
}

.login-page.is-dark {
  background:
    radial-gradient(circle at 18% 12%, rgba(139, 71, 234, 0.28), transparent 30rem),
    radial-gradient(circle at 84% 18%, rgba(6, 182, 212, 0.16), transparent 28rem),
    radial-gradient(circle at 72% 86%, rgba(16, 185, 129, 0.12), transparent 24rem),
    linear-gradient(135deg, #0f1020 0%, #17132a 52%, #0d1725 100%);
}

.login-texture {
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(114, 46, 209, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(114, 46, 209, 0.07) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(circle at center, black, transparent 72%);
}

.login-shell {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-width: 0;
  margin: auto;
}

.login-panel {
  width: min(100%, 460px);
  padding: clamp(22px, 4vw, 34px);
  border: 1px solid rgba(114, 46, 209, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 28px 70px rgba(79, 70, 229, 0.18);
  backdrop-filter: blur(22px);
}

.is-dark .login-panel {
  border-color: rgba(139, 71, 234, 0.35);
  background: rgba(18, 18, 32, 0.78);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.34);
}

.login-panel-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
}

.login-brand {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.login-logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  flex: 0 0 auto;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.16), rgba(6, 182, 212, 0.12));
  box-shadow: inset 0 0 0 1px rgba(114, 46, 209, 0.16);
}

.login-logo-mark img {
  width: 42px;
  height: 42px;
  object-fit: contain;
}

.login-title-block {
  min-width: 0;
}

.login-title-block p {
  margin: 0 0 2px;
  color: #722ed1;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.login-title-block h1 {
  margin: 0;
  overflow-wrap: anywhere;
  color: #21123b;
  font-size: clamp(26px, 4vw, 34px);
  font-weight: 900;
  line-height: 1.08;
}

.is-dark .login-title-block h1 {
  color: #f7f2ff;
}

.login-language-select {
  width: 132px;
  flex: 0 0 auto;
}

.login-subtitle {
  margin: 14px 0 22px;
  color: #5f5474;
  font-size: 14px;
  font-weight: 600;
}

.is-dark .login-subtitle {
  color: #d8d1e7;
}

.login-segmented {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  min-width: 0;
  margin-bottom: 22px;
  padding: 5px;
  border: 1px solid rgba(114, 46, 209, 0.13);
  border-radius: 18px;
  background: rgba(114, 46, 209, 0.07);
}

.login-segment {
  min-width: 0;
  height: 42px;
  overflow: hidden;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: #423158;
  cursor: pointer;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition:
    color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.login-segment:hover {
  color: #722ed1;
  background: rgba(255, 255, 255, 0.62);
}

.login-segment.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  box-shadow: 0 10px 24px rgba(114, 46, 209, 0.28);
}

.login-segment:focus-visible,
.password-toggle:focus-visible,
:deep(.login-submit.n-button:focus-visible) {
  outline: 3px solid rgba(114, 46, 209, 0.3);
  outline-offset: 3px;
}

.is-dark .login-segment {
  color: #e8ddff;
}

.is-dark .login-segment:hover {
  background: rgba(139, 71, 234, 0.24);
  color: #ffffff;
}

:deep(.n-form-item-label__text) {
  color: #2f2145;
  font-size: 14px;
  font-weight: 800;
}

.is-dark :deep(.n-form-item-label__text) {
  color: #eee7ff;
}

.login-form :deep(.n-input),
.login-form :deep(.n-base-selection) {
  border-radius: 16px;
}

.login-form :deep(.n-input .n-input__border),
.login-form :deep(.n-input .n-input__state-border),
.login-form :deep(.n-base-selection .n-base-selection__border),
.login-form :deep(.n-base-selection .n-base-selection__state-border) {
  border-radius: 16px;
}

.login-form :deep(.n-input.n-input--focus .n-input__state-border),
.login-form :deep(.n-base-selection.n-base-selection--active .n-base-selection__state-border) {
  border-color: #722ed1;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.14);
}

.password-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #6d5a83;
  cursor: pointer;
  transition:
    color 180ms ease,
    background-color 180ms ease;
}

.password-toggle:hover {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.login-submit-row {
  margin-top: 6px;
}

:deep(.login-submit.n-button) {
  height: 46px;
  border-radius: 16px;
  font-weight: 900;
  box-shadow: 0 14px 30px rgba(114, 46, 209, 0.24);
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    opacity 180ms ease;
}

:deep(.login-submit.n-button:not(.n-button--disabled):hover) {
  transform: translateY(-1px);
  box-shadow: 0 18px 36px rgba(114, 46, 209, 0.3);
}

@media (max-width: 560px) {
  .login-page {
    padding: 14px;
  }

  .login-panel {
    border-radius: 20px;
  }

  .login-panel-top {
    flex-direction: column;
    align-items: stretch;
  }

  .login-language-select {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-segment,
  .password-toggle,
  :deep(.login-submit.n-button) {
    transition: none;
  }
}
</style>
