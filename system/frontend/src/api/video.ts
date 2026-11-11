import requert from "@/utils/fetch"

export const createVideo = (data: any) => requert({ url: "/video/add", method: "post", data }, true);  
export const updateVideo = (data: any) => requert({ url: "/video/update", method: "post", data }, true);
export const getVideoInfo = (data: any) => requert({ url: "/video/getVideoById", data })
export const deleteVideo = (data: any) => requert({ url: "/video/delete", data }, true)
export const getVideoList = (data: any) => requert({ url: "/video/list", data })

export const getCoursePlayList = (data: any) => requert({ url: "/course/getRecordList", data })
export const playVideo = (data: any) => requert({ url: "/course/play", data })
export const updetaVideoTime = (data: any) => requert({ url: "/course/updatePlayingTime", data })
export const getCourseList = (data: any) => requert({ url: "/course/getCourseList", data })
export const addVideoRate = (data: any) => requert({url:`/course/hits`,data})
