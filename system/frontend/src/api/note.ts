import request from "@/utils/fetch"
export const saveNote = (data: any) => request({ url: "/notes/addOrUpdateNotes", method: "post", data })
export const getNote = (data: any) => request({ url: "/notes/getNotesByVideoId", method: "get", data })
export const editNote = (data: any) => request({url:"/notes/addOrUpdateNotes", method:"post",data})
