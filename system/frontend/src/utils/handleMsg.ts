
import i18n from "@/lang"
const handleMsg = (pageData: any, show: boolean) => {
  if (pageData.code === 200) {
    show && window.$msg.success(pageData.msg || 'ok')
  } else if (pageData.code === 401 || pageData.code === 1002 || pageData.code === 1003 || pageData.code === 1004) {
    window.$msg.error(pageData.msg || '请求错误')
    localStorage.removeItem("token")
    location.reload()
  } else if (pageData.code === 500) {
    window.$msg.error(i18n.global.t('system.serverErr'))
  } else {
    window.$msg.error(pageData.msg || '请求错误')
  }

}

export default handleMsg
