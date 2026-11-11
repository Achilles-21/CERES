import request from "@/utils/fetch"
export const getAnalysisList = (data: any) => request({ url: "/case/analysis", method: "post", data })
export const getModalQueList = (data: any) => request({ url: "/case/getQue", method: "post", data })
export const getAnswer = (data: any) => request({ url: "/case/evaluate", method: "post", data })
export const showAnswer = (data: any) => request({ url: "/case/showAnswers", method: "get", data })
