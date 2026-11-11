import router from '@/router'
import useSystemStore from '@/store/useSystemStore'
import settings from '@/settings'
import duration from './duration'
import i18n from '@/lang'
import { canUseSlideGen } from '@/utils/slideGenAccess'

const access = ['/home', '/test' , '/course', '/study', '/testCate', '/test']

const blockSlideGenIfUnauthorized = (to: { path: string }, systemStore: ReturnType<typeof useSystemStore>) => {
  if (to.path === '/slideGen' && !canUseSlideGen(systemStore.userInfo)) {
    window.$msg.error(i18n.global.t('userPage.notAccess'))
    return true
  }
  return false
}

router.beforeEach(async (to, from, next) => {
  duration.calc(from, to)
  const systemStore = useSystemStore()
  const token = localStorage.getItem('token')
  window.$bar.start()
  if (token) {
    const _path = to.fullPath.split('?')[0]
    if(systemStore.userInfo.level == '1' && access.indexOf(_path) == -1) {
      window.$msg.error(i18n.global.t('userPage.notAccess'))
      next('/')
      return
    }
    if (!systemStore.routesAdded) {
       await systemStore.getUserInfo();
      systemStore.routesAdded = true;
      if (blockSlideGenIfUnauthorized(to, systemStore)) {
        next('/home')
        return
      }
      next({ ...to, replace: true });
    } else {
      if (to.path === '/login') {
        next('/');
      } else if (blockSlideGenIfUnauthorized(to, systemStore)) {
        next('/home')
      } else {
        next();
      }
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login`)
    }
  }
})
router.afterEach((to,from,next)=>{
  window.$bar.finish()
  document.title =settings.title
})
