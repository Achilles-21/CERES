import api from '@/api';
import mitt from 'mitt';
import router from "@/router"
import store from '@/store';
import i18n from '@/lang';
import 'virtual:svg-icons-register'
import "./naive"
import "@/router/permission"
import { handleColorReversal, toHexToRGB } from "@/utils/handleColor"
import useSystemStore from '@/store/useSystemStore';
declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $api: Api;
    $mitt: Mitt;
    $qf: (color: string, ilighten: number) => string;
    $htor: (color: string, opacity: number) => string;
  }
}
const init = (app: any) => {
  app.use(router)
  app.use(store)
  app.use(i18n)
  app.config.globalProperties.$api = api
  app.config.globalProperties.$mitt = mitt()
  app.config.globalProperties.$qf = handleColorReversal
  app.config.globalProperties.$htor = toHexToRGB
}
export default init
