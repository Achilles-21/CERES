
import requert from "@/utils/fetch"


export const createImage = (data: any) => requert({ url: "/image/add", method: "post", data }, true);  
export const updateImage = (data: any) => requert({ url: "/image/update", method: "post", data }, true);
export const getImageInfo = (data: any) => requert({ url: "/image/getImageById", data })
export const deleteImage = (data: any) => requert({ url: "/image/delete", data }, true)
export const getImageList =(data:any) => requert({url:"/image/list",data})
