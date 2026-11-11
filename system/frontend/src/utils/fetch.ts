import handleMsg from "./handleMsg"
const baseUrl = (import.meta.env.VITE_APP_URL || '/apii') 

interface FetchOptions {
  url: string
  method?: string
  data?: Record<string, any>
  headers?: Record<string, string>
  show: boolean
}

const queryString = (params: Record<string, any>): string => {
  return Object.keys(params).length > 0
    ? '?' +
        Object.keys(params)
          .map((key) => `${key}=${encodeURIComponent(params[key])}`)
          .join('&')
    : ''
}

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

const handleResponse = async (response: Response, show: boolean): Promise<any> => {

  const pageData = await response.json()
  const modifiedData = replaceUrlInObject(pageData)
  if (response.ok) {
    handleMsg(modifiedData ,show)
    return modifiedData
  } else {
    window.$msg.error(pageData.code + ':'+ pageData.msg || pageData.code + ':'+ '请求错误')
    throw new Error(pageData.code + ':' + (pageData.msg || '请求错误'))
  }
}


const fetchData = async ({ url, method = 'GET', data = {}, headers = {}, }: FetchOptions, show = false : boolean): Promise<any> => {
  try {
    const options: RequestInit = {
      method: method.toUpperCase(),
      credentials: 'include',  
      headers: {
        'Authorization': localStorage.getItem('token'),
        'Content-Type': 'application/json;charset=UTF-8',
        ...headers,
      },
    }

    if (options.method === 'GET') {
      url = url + queryString(data)
    } else {
      options.body = JSON.stringify(data)
    }
    const response = await fetch(baseUrl + url, options)
    return await handleResponse(response, show)
  } catch (err: any) {
    window.$msg.error(err.message || '发送请求错误')
    return new Error(err.message || '发送请求错误')
  }
}

export default fetchData
