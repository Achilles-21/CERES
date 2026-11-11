import requert from "@/utils/fetch"
export const getUserinfo = () => requert({url:'/getInfo'})

export const getUserList = (data) => requert({ url: "/system/user/list",data })
export const addUser = (data) => requert({url:"/system/user", method:"post",data})
export const editUser = (data) => requert({url:"/system/user/update",method:"post",data})
export const deleteUser = (data) => requert({url:`/system/user/delete/${data.userId}`,method:"delete"},true)
