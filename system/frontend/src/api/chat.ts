
import requert from "@/utils/fetch"

export const getChatList = (data: any) => requert({ url: "/chat", method:"post",data });  
export const audioToText = (data: any) => requert({url:"/chat/audioTrans2Text",method:"post",data});  
export const textToAudio = (data: any) => requert({url:"/chat/text2audio",method:"post",data});  
export const clearChat = (data: any) => requert({url:"/chat/clear",method:"post",data});  
