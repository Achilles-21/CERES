import requert from "@/utils/fetch"

export const getTeacherList = (id: any) => requert({ url: `/teacher/image/list/${id}` })
export const addTeacher = (data: any) => requert({ url: "/teacher/image/add", method: "post", data })
export const upTeacher = (data: any) => requert({ url: "/teacher/image/update", method: "put", data })
export const deleteTeacher = (id) => requert({ url: `/teacher/image/delete/${id}`, method: "delete" })
export const getTeacher = (id: any) => requert({ url: `/teacher/image/${id}`, })
