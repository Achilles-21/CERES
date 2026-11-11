import requert from "@/utils/fetch"

export const sendForum = (data: any) => requert({ url: "/invitation/add", method: "post", data })
export const getForumList = (data: any) => requert({ url: "/invitation/pageQuery", method: "get", data })
export const getForumInfo = (id: any) => requert({ url: `/invitation/selectById/${id}` })
export const delForum = (id: any) => requert({ url: `/invitation/delete/${id}`, method: "delete" })
export const sendComment = (data: any) => requert({ url: "/invitationReply/add", method: "post", data })
export const delComment = (id: any) => requert({ url: `/invitationReply/delete/${id}`, method: "delete" })
export const getCommentList = (data: any) => requert({ url: "/invitationReply/pageQuery", data })
export const getAllCommentList = (data: any) => requert({ url: "/invitationReply/listComment", data })
export const addLike = (data: any) => requert({ url: "/invitation/likes", method: "post", data })
export const getCommentInfo = (id) => requert({url:`/invitation/selectById/${id}`})
