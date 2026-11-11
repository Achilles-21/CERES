import { createI18n } from 'vue-i18n'
import enUs from './enUS'
import zhCn from './zhCn'
import malay from './malay'
import hindi from './hindi'
const lang = localStorage.getItem('lang') || 'zhcn'
const i18n = createI18n({
  locale: lang,
  fallbackLocale: lang,
  messages: {
    zhcn:zhCn,
    enus:enUs,
    malay,
    hindi,
  },
})

export default i18n
