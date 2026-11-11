
import i18n from "@/lang"
import img2 from "@/assets/image/2.svg"
import img3 from "@/assets/image/3.svg"
import img4 from "@/assets/image/4.svg"
import img5 from "@/assets/image/5.svg"
import img6 from "@/assets/image/6.svg"
import img7 from "@/assets/image/7.svg"
import img8 from "@/assets/image/8.svg"
import img9 from "@/assets/image/9.svg"
import { listAll } from "@/api/question"
import { getVideoOption } from "@/api/bank"
export const useImageAddForm = () => {
  const formData = ref({})
  const addImageFormItem = computed(() => (

    [
      { label: i18n.global.t('image.imageUpload'), path: 'fileUrl', type: 'upload', itemwidth: "100%", headers: { 'Authorization':localStorage.getItem("token") }, data:{folderName:"image"},accept:"image/*",max:1,url: `${import.meta.env.VITE_APP_URL}/file/upload`,placeholder: i18n.global.t('image.imageUploadTips') },
      { label: i18n.global.t('image.imageDescription'), path: 'description', itemwidth: '100%',max: 1, type: 'input', nativeType: "textarea", placeholder: i18n.global.t('image.imageDescriptionTips') },
    ]
  ))
  return {
    formData,
    addImageFormItem
  }
}

 const queryVideoListFormItem = [
  { label: i18n.global.t('video.courseName'), path: 'name', type: 'input',  placeholder: i18n.global.t('video.courseNameTips') },
]
export const useQueryVideoListForm = () => {
  const formData = ref({})
  return {
    formData,
    queryVideoListFormItem
  }
}

export const useVideoAddForm = () => {
  const formData = ref({})
  const options = [{ label: i18n.global.t('video.show'), value: '1' }, { label: i18n.global.t('video.hide'), value: '0' }]

  const addVideoFormItem = computed(() => [
    { label: i18n.global.t('video.courseName'), path: 'name', itemwidth: '100%', type: 'input',  placeholder: i18n.global.t('video.courseNameTips') },
    { label: i18n.global.t('video.isShow'), path: 'isShow', itemwidth: '100%', type: 'radio', options,  placeholder: i18n.global.t('video.isShowTips') },
    { label: i18n.global.t('video.videoUpload'), path: 'fileUrl', type: 'upload', itemwidth: "100%", data: { folderName: "video" }, headers: { 'Authorization': localStorage.getItem("token") }, accept: "video/*", max: 1, url: `${import.meta.env.VITE_APP_URL}/file/upload/video`, placeholder: i18n.global.t('video.videoUploadTips') },
    { label: i18n.global.t('video.videoCover'), path: 'imageUrl', type: 'upload', itemwidth: "100%", headers: { 'Authorization': localStorage.getItem("token") }, data: { folderName: "image" }, accept: "image/*", max: 1, url: `${import.meta.env.VITE_APP_URL}/file/upload`, placeholder: i18n.global.t('video.videoCoverTips') },
    { label: "PDF", path: 'pptUrl', type: 'upload', itemwidth: "100%", headers: { 'Authorization':localStorage.getItem("token") }, data:{folderName:"pdf"},accept:".pdf",max:1,url: `${import.meta.env.VITE_APP_URL}/file/upload`,placeholder: i18n.global.t('video.pptTips') },
    { label: i18n.global.t('video.duration'), path: 'duration', disabled: false, itemwidth: '100%', type: 'input', placeholder: i18n.global.t('video.durationTips') },
    { label: i18n.global.t('video.videoDescription'), path: 'description', itemwidth: '100%', type: 'input', nativeType: "textarea", placeholder: i18n.global.t('video.videoDescriptionTips') },

  ])


  return {
    formData,
    addVideoFormItem
  }
}

const questionAnswerOption =[
  { label: "A", value: "A" },
  { label: "B", value: "B" },
  { label: "C", value: "C" },
  { label: "D", value: "D" },
  { label: "E", value: "E" },
  { label: "F", value: "F" }]
const judgeOption=[{ label: "正确", value: true },{ label: "错误", value: false }]
export const useQuestionAddFrom = () => {
  const formData = ref({
    type:0,
  })
  const addQuestionFormItem = computed(() => ([
    { label: '类型:', path: 'type', type: 'radio', itemwidth: "100%", options: [{ label: "单选题", value: "0" }, { label: "多选题", value: "1" }, { label: "问答题", value: "2" } ] },
    { label: i18n.global.t('image.image'), path: 'imageUrl', type: 'upload', itemwidth: "100%", headers: { 'Authorization':localStorage.getItem("token") }, data:{folderName:"image"},accept:"image/*",max:1,url: `${import.meta.env.VITE_APP_URL}/file/upload`,placeholder: i18n.global.t('image.imageUploadTips') },
    { label: i18n.global.t('question.question'), path: 'content', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.questionTips') },
    { label: 'A', path: 'choiceA', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'A', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: 'B', path: 'choiceB', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'B', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: 'C', path: 'choiceC', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'C', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: 'D', path: 'choiceD', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'D', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: 'E', path: 'choiceE', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'E', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: 'F', path: 'choiceF', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.optionsTips')+'F', isShow: formData.value.type == "0" || formData.value.type == "1" },
    { label: i18n.global.t('question.answer'), path: 'answer', type:'radio', itemwidth: "100%", options:questionAnswerOption, isShow: formData.value.type == "0" },
    { label: i18n.global.t('question.answer'), path: 'answer', type: 'check', itemwidth: "100%", options: questionAnswerOption, isShow: formData.value.type == "1" },
    { label: formData.value.type == "2" ? i18n.global.t('question.answer') : i18n.global.t('question.analysis'), path: 'answerExplain', itemwidth: '100%', type: 'input', nativeType: "textarea", placeholder: i18n.global.t('question.analysisTips') },
    { label: i18n.global.t('question.questionDescription'), path: 'description', itemwidth: '100%', type: 'input', nativeType: "textarea", placeholder: i18n.global.t('question.uestionDescriptionTips') },
  ]))

  return {
    formData,
    addQuestionFormItem
  }
}

export const useUserQueryForm = () => {
  const {proxy} = getCurrentInstance()
  const userQueryFormItem = computed(()=>[
    { label: proxy.$t('userPage.name'), path: 'a1',  type: 'input', placeholder: proxy.$t('userPage.nameTips') },
    { label: proxy.$t('login.account'), path: 'a2', type: 'input',  placeholder: proxy.$t('login.accountTips') },
  ])
  const queryFormData = ref({})
  return {
    queryFormData,
    userQueryFormItem
  }
}


export const useAddUserForm = () => {
  const formData = ref({})
  const userAddFormItem = computed(()=>[
    { label: i18n.global.t('userPage.name'), path: 'a1', type: 'input', placeholder: i18n.global.t('userPage.nameTips') },
    { label: i18n.global.t('login.account'), path: 'a2', type: 'input', placeholder: i18n.global.t('login.accountTips') },
    { label: i18n.global.t('login.password'), path: 'a3', type: 'input', placeholder: i18n.global.t('login.passwordTips') },
  ])
  return {
    formData,
    userAddFormItem
  }
}


export const adminUserQueryForm = () => {
  const {proxy} = getCurrentInstance()
  const queryFormData = ref({})

const adminQueryFormItem = computed(()=>[
  { label: proxy.$t('userPage.name'), path: 'username',  type: 'input', placeholder: proxy.$t('userPage.nameTips') },
])

  return {
    queryFormData,
    adminQueryFormItem
  }
}


export const useAddAdminForm = () => {
  const formData = ref({})
  const adminAddFormItem = computed(()=>[
    { label: i18n.global.t('userPage.name'), path: 'username', type: 'input', placeholder: i18n.global.t('userPage.nameTips') },
      { label: i18n.global.t('login.account'), path: 'phonenumber', type: 'input', placeholder: i18n.global.t('login.accountTips') },
      { label: i18n.global.t('login.password'), path: 'password', type: 'input', placeholder: i18n.global.t('login.passwordTips') },
      {
        label: i18n.global.t('userPage.level'), path: 'level', type: 'select', placeholder: i18n.global.t('userPage.roleTips'), options: [{
          label: i18n.global.t('userPage.level0'),
          value:"0"
        }, {
          label: i18n.global.t('userPage.level1'),
          value:"1"
      }] },
    {
      label: i18n.global.t('userPage.role'), path: 'roleId', type: 'select', placeholder: i18n.global.t('userPage.roleTips'), options: [{
        label: "前台用户",
        value:"3"
      }, {
        label: "后台用户",
        value:"2"
    }] },
  ])
  return {
    formData,
    adminAddFormItem
  }
}


export const useAddTeacherForm = () => {
  const formData = ref({})
  const addTeacherFormItem = computed(() => (
    [
      { label: i18n.global.t('teacher.teacherImage'), path: 'teacherImage', type: 'upload', itemwidth: "100%", headers: { 'Authorization': localStorage.getItem("token") }, data: { folderName: "image" }, accept: "image/*", max: 1, url: `${import.meta.env.VITE_APP_URL}/file/upload`, placeholder: i18n.global.t('teacher.teacherImageTips') },
      { label: i18n.global.t('teacher.videoNode'), path: 'imageTime', itemwidth: '100%', type: 'input',  placeholder: i18n.global.t('teacher.videoNodeTips') },
    ]
  ))
  return {
    formData,
    addTeacherFormItem
  }
}

const patientOption = [
  { label: h('img', { src: img2,class:"w-[80px] h-[80px]" }), value: "2" },
  { label: h('img', { src: img3,class:"w-[80px] h-[80px]" }), value: "3" },
  { label: h('img', { src: img4,class:"w-[80px] h-[80px]" }), value: "4" },
  { label: h('img', { src: img5,class:"w-[80px] h-[80px]" }), value: "5" },
  { label: h('img', { src: img6,class:"w-[80px] h-[80px]" }), value: "6" },
  { label: h('img', { src: img7,class:"w-[80px] h-[80px]" }), value: "7" },
  { label: h('img', { src: img8,class:"w-[80px] h-[80px]" }), value: "8" },
  { label: h('img', { src: img9,class:"w-[80px] h-[80px]" }), value: "9" },
]
export const useAddCasebook = () => {
  const formData = ref({})
  const addCasebookFormItem = computed(() => (
    [
      { label: i18n.global.t('casebook.caseName'), path: 'name', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.caseNameTips') },
      { label: i18n.global.t('casebook.queNum'), path: 'questionsNumber', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queNumTips') },
      { label: i18n.global.t('casebook.question', { num: 1 }), path: 'question1', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 1 }) },
      { label: i18n.global.t('casebook.answer', { num: 1 }), path: 'answer1', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 1 }) },
      { label: i18n.global.t('casebook.question', { num: 2 }), path: 'question2', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 2 }) },
      { label: i18n.global.t('casebook.answer', { num: 2 }), path: 'answer2', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 2 }) },
      { label: i18n.global.t('casebook.question', { num: 3 }), path: 'question3', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 3 }) },
      { label: i18n.global.t('casebook.answer', { num: 3 }), path: 'answer3', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 3 }) },
      { label: i18n.global.t('casebook.question', { num: 4 }), path: 'question4', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 4 }) },
      { label: i18n.global.t('casebook.answer', { num: 4 }), path: 'answer4', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 4 }) },
      { label: i18n.global.t('casebook.question', { num: 5 }), path: 'question5', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 5 }) },
      { label: i18n.global.t('casebook.answer', { num: 5 }), path: 'answer5', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 5 }) },
      { label: i18n.global.t('casebook.question', { num: 6 }), path: 'question6', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.queTips', { num: 6 }) },
      { label: i18n.global.t('casebook.answer', { num: 6 }), path: 'answer6', itemwidth: '100%', type: 'input', placeholder: i18n.global.t('casebook.answerTips', { num: 6 }) },
      { label: i18n.global.t('casebook.patientImage'), path: 'patientImage', type: 'radio', itemwidth: "100%", options: patientOption,placeholder: i18n.global.t('casebook.patientImageTips') },
      { label: i18n.global.t('casebook.caseDescription'), path: 'patient', itemwidth: '100%', type: 'input', nativeType: "textarea", placeholder: i18n.global.t('casebook.caseDescriptionTips') },
      { label: i18n.global.t('casebook.caseImage'), path: 'casePicsList', type: 'upload',multiple:true, itemwidth: "100%", headers: { 'Authorization': localStorage.getItem("token") }, data: { folderName: "image" }, accept: "image/*",  url: `${import.meta.env.VITE_APP_URL}/file/upload`, placeholder: i18n.global.t('casebook.caseImageTips') },
    ]
  ))
  return {
    formData,
    addCasebookFormItem
  }
}

export const useAddBankForm = () => {
  const formData = ref({})
  const option = ref([])
  const videoOption = ref([])
  const showOption = [
    { label: '隐藏', value: 0 },
    { label: '显示', value: 1 },
  ]
  const addBankFormItem = computed(() => [
    { label: '显示弹窗', path: 'isShow', type: 'radio', itemwidth: "100%", options: showOption, placeholder: '' },
    { label: i18n.global.t('question.pauseTime'), path: 'questionTime',  itemwidth: '100%', type: 'input', placeholder: i18n.global.t('question.pauseTimeTips') },
    { label: i18n.global.t('question.question'), path: 'queId', itemwidth: '100%', multiple: true, type: 'select', options: option.value, filterable: true, placeholder: i18n.global.t('question.selectQuestionTips') },
    { label: '视频', path: 'popVideoId', itemwidth: '100%', type: 'select',clearable:true,options:videoOption.value,filterable:true, placeholder: i18n.global.t('question.selectQuestionTips') },
  ])
  const getOption = async () => {
    const res = await listAll()
    if (res.code === 200) {
      res.data.forEach(element => {
        element.label = '【' + element.value + '】' + element.label
      });
      option.value = res.data
    }
  }
  const getVideoOptions = async () => {
    const res = await getVideoOption()
    if (res.code === 200) {
      videoOption.value = res.data
    }
  }
  onMounted(() => {
    getOption()
    getVideoOptions()
  })
  return {
    formData,
    addBankFormItem
  }
}
