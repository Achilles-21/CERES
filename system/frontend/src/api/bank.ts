import request from "@/utils/fetch"

export const getVideoOption = () => request({url:"/video/listAll",method:"get"})
export const getBankList = (data: any) => request({ url: "/videoPopQue/page", method: "get", data })
export const getBankInfo = (data: any) => request({url:"/videoPopQue/getById",data})
export const addBank = (data: any) => request({ url: "/videoPopQue/add", method: "post", data })
export const upBank = (data: any) => request({ url: "/videoPopQue/update", method: "post", data })
export const delBank = (data:any)=> request({url:`/videoPopQue/delete/${data}`,method:"delete"})
