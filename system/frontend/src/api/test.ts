import requert from "@/utils/fetch"
export const createTest = (data: any) => requert({ url: "/exam/addExam", method: "post", data }, true)
export const updateTest = (data: any) => requert({ url: "/exam/update", method: "post", data }, true)
export const deleteTest = (data: any) => requert({ url: "/exam/delete", data }, true)
export const getTestList = (data: any) => requert({ url: "/exam/getExamList", data })
export const getTestInfo = (data: any) => requert({ url: "/exam/getExamById", data })

export const getExamList = (data: any, cateId: number) => requert({ url: "/exam/practice" + "?cateId=" + cateId, method: "post", data })
export const sendAnswerList = (data: any) => requert({ url: "/exam/saveExamRecordList", method: "post", data })
export const listAll = (data: any) => requert({ url: "/exam/listAll" })

export const createTestCate = (data: any) => requert({ url: "/exam/addExamCate", method: "post", data }, true)
export const updateTestCate = (data: any) => requert({ url: "/exam/updateCate", method: "post", data }, true)
export const deleteTestCate = (data: any) => requert({ url: "/exam/deleteCate", data }, true)
export const getTestCateList = (data: any) => requert({ url: "/exam/getExamCateList", data })
export const getTestIdList = (data: any) => requert({ url: "/exam/getExamIdList?cateId=" + data})
export const getFrontTestCateList = (data: any) => requert({ url: "/exam/getFrontExamCateList", data })
