import { defineStore } from 'pinia'
import { getUserinfo } from '@/api/user'
import { getLanguageOption } from '@/api/system'
import router, { pageRoutes, noLoyoutRoutes, asyncPageRoutes, staticRoutes, adminRoutes, userRoutes,resetRouter } from '@/router'
import { darkTheme } from 'naive-ui'
import { markRaw } from 'vue'
import i18n from '@/lang';

const syncDocumentTheme = (isDark: boolean) => {
  if (typeof document === 'undefined') return
  document.documentElement.classList.toggle('ceres-dark', isDark)
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light'
}

const theme = localStorage.getItem('theme') ? markRaw(darkTheme) : null
syncDocumentTheme(Boolean(theme))
import t1 from "@/assets/image/t1.svg"
import t2 from "@/assets/image/t2.svg"
import t3 from "@/assets/image/t3.svg"
import t4 from "@/assets/image/t4.svg"
const useSystemStore = defineStore('system', {
  state: () => ({
    layout: 'level',  
    permissions: [],
    roles: [],
    userInfo: {},
    defaultRouter:[],
    routes: [],
    routesAdded: false,
    theme: theme ,  
    pc: true,
    collapsed: false,
    prot: "0",
    lang: localStorage.getItem('lang')||'zhcn',
    languageOption:[],
    showDurationLayout: false,  
    group: '',  
  }),
  actions: {
    changeLayout() {
      this.layout = this.layout === 'vertical' ? 'level' : 'vertical'
    },
    async getUserInfo() {
      const res = await getUserinfo()
      this.permissions = res.permissions
      this.roles = res.roles
      res.user.teacherPicture = res.user.teacherPicture.toString()
      res.user.ps = {
        "1": t1,
        "2": t2,
        "3": t3,
        "4": t4
      }
      this.userInfo=res.user
      this.addRoutes()
    },
    async getLangOption() {
      const res = await getLanguageOption()
      this.languageOption = res.data.map(item => ({ label: item.dictLabel, value: item.dictValue }))
      this.lang = localStorage.getItem('lang') || this.languageOption.find(item => item.label ==='中文（简体）').value
    },
    changeTheme() {
      const nextTheme = this.theme != null ? null : markRaw(darkTheme)
      if (nextTheme) {
        localStorage.setItem('theme', 'dark')
      } else {
        localStorage.removeItem('theme')
      }
      this.theme = nextTheme
      syncDocumentTheme(Boolean(nextTheme))
    },
    changeLang(v) {
      i18n.global.locale=v
      this.lang = v
      localStorage.setItem('lang', v)
    },
    addRoutes() {


          const childrenRoutes = this.prot =='0'?userRoutes:adminRoutes
      this.defaultRouter=childrenRoutes[0].children
      const routes = [...childrenRoutes,...staticRoutes]
      routes.forEach((item) => {
        router.addRoute(item)
      })

      if (this.prot == '1') {
       this.routes = this.handleNav(childrenRoutes[0].children)
       }
    },
    handlePermissionsRoutes(routes) {
      return routes.filter(item => {
        if (item.children && item.children.length > 0) {
          item.children = this.handlePermissionsRoutes(item.children)
        }
        return this.permissions.includes(item.name)
      })
    },
    handleNav(routes) {
      return routes.map((item) => {
        if(item.hidden) return
        let p = { label: i18n.global.t(item.meta.title), key: item.path }
          item.meta.icon? p.icon = () => h('span', { class: `iconify ${item.meta.icon}` }):''
        if (item.children && item.children.length > 0) {
          if (item.children.length == 1) {
              item.children[0].meta.icon? p.icon = () => h('span', { class: `iconify ${item.children[0].meta.icon}` }):''
            p.title = i18n.global.t(item.children[0].meta.title)
            p.key = item.children[0].path
          } else {
            p.children = this.handleNav(item.children)
          }
        }
        return p
      }).filter(Boolean)
    },
    setToken() {
      localStorage.setItem('token', '123456')
    },
    resetStore() {
      this.$reset()
      resetRouter()
    }
  },
  persist: {
    key: 'system',
    storage: localStorage,
    paths: ['layout',"prot","lang"],
  },
})
export default useSystemStore
