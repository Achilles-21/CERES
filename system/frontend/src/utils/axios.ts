import axios from 'axios'
import handleMsg from "./handleMsg"
const service = axios.create({
  baseURL:(import.meta.env.VITE_APP_URL || '/apii'),  
  withCredentials: true,  
  timeout: 5000  
})

service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.log(error)  
    return Promise.reject(error)
  }
)

const replaceUrlInObject = (obj: any) => {
  if (typeof obj === 'string') {
    const mediaBase = import.meta.env.VITE_MEDIA_PUBLIC_BASE || 'http://localhost:10008/ceres'
    return obj.startsWith(mediaBase) ? '/staticc' + obj.slice(mediaBase.length) : obj
  }
  if (Array.isArray(obj)) {
    return obj.map(item => replaceUrlInObject(item))
  }
  if (typeof obj === 'object' && obj !== null) {
    return Object.keys(obj).reduce((acc, key) => {
      acc[key] = replaceUrlInObject(obj[key])
      return acc
    }, {} as Record<string, any>)
  }
  return obj
}

service.interceptors.response.use(
  response => {
    const config = response.config;
    const res = response.data
    const modifiedData = replaceUrlInObject(res)
    handleMsg(modifiedData, config.show)
    return modifiedData
  },
  error => {
    message.error(error.ElMessage)
    return Promise.reject(error)
  }
)

export default service
