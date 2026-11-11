import requert from "@/utils/fetch"

export const addCasebook = (data: any) => requert({ url: "/case/add", method: "post", data })
export const upCasebook = (data: any) => requert({ url: "/case/update", method: "put", data })
export const getCasebookList = (data: any) => requert({ url: "/case/page", method: "get", data })
export const delCasebook = (id: any) => requert({ url: `/case/del/${id}`, method: "delete" })
export const getCasebook = (id: any) => requert({ url: `/case/${id}`, method: "get" })
