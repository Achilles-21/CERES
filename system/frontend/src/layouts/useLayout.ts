import { storeToRefs } from 'pinia'
import useSystemStore from '@/store/useSystemStore'
const useLayout = () => {
  const { push } = useRouter()
  const r = useRoute()
  const path = ref('')
  const systemStore = useSystemStore()
  const { routes, collapsed, pc, lang, defaultRouter } = storeToRefs(systemStore)
  const isDark = computed(() => Boolean(systemStore.theme))
  const menuThemeOverrides = computed(() => ({
    itemColorHover: 'rgba(114, 46, 209, 0.08)',
    itemColorActive: 'rgba(114, 46, 209, 0.13)',
    itemColorActiveHover: 'rgba(114, 46, 209, 0.16)',
    itemColorActiveCollapsed: 'rgba(114, 46, 209, 0.13)',
    itemTextColorHover: '#722ed1',
    itemTextColorActive: '#5b21b6',
    itemTextColorActiveHover: '#5b21b6',
    itemIconColorHover: '#722ed1',
    itemIconColorActive: '#722ed1',
    itemIconColorActiveHover: '#722ed1',
    borderRadius: '10px',
  }))
  const onUpdareValue = (key: string) => {
    if (!pc.value) {
      collapsed.value = false
    }
    if (key && key !== r.path) {
      push(key)
    }
  }
  const renderMenuIcon = (option: NaiveUI.MenuOption) => {
    return option.icon ? h(option.icon) : false
  }
  watch(() => r.path, () => {
    path.value = r.path
  }, { immediate: true })
  watch(() => lang.value, () => {
    routes.value = systemStore.handleNav(defaultRouter.value)
  })
  return {
    routes,
    collapsed,
    path,
    onUpdareValue,
    renderMenuIcon,
    pc,
    isDark,
    menuThemeOverrides,
  }
}
export default useLayout
