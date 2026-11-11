import useSystemStore from '@/store/useSystemStore'

const useChart = () => {
  const {proxy}:{proxy:CusProxy} = getCurrentInstance() as any
  const msgList = ref({})
  const { path} = useRoute()
  const systemStore = useSystemStore()
  const activeLanguage = computed(() => String(systemStore.lang || localStorage.getItem('lang') || 'zhcn'))
  const modelType = path === '/exam' ? 2 : 0
  const getChatList = async () => {
    const res = await proxy.$api.chat.getChatList({ isQue: 0, modelType, language: activeLanguage.value })
    msgList.value = res.data
  }
  const clearMessages = () => { msgList.value = [] }
  const onEnter = async (val, base,id,content,answer,isQue,queId,modelType=0) => {
    msgList.value = msgList.value.concat([
      {
        "role": "user",
        "content": val,
        "temp": true
      },
      {
        "role": "system",
        "content": "",
        "picture": 1,
        "loading": true,
        "temp": true
      }
    ])
    const popId = document.getElementById('popId')?.value

    const res = await proxy.$api.chat.getChatList({ query: val, image: base, isQue, id, content, answer, userAnswer: val, queId, modelType, popId, language: activeLanguage.value })

    if (res.code == 200) {
      if (modelType === 2) {
        proxy.$mitt.emit('sendQue',res.data[res.data.length-1].recommendQueRespList)
      }
      res.data.forEach((item:any) => {
        if (item.isPop === '0') {
          proxy.$mitt.emit("setVideo", {
            src:item.videoUrl,
            isPop:item.isPop,  
            userAnswer:val,
          })
        }
        for (let i = msgList.value.length - 1; i >= 0; i--) {
          if(msgList.value[i].temp) {
            msgList.value.splice(i, 1)
            continue
          }
        }
        msgList.value.push(item)
      })
    }
  }
  onMounted(() => {
    getChatList()
  })
  return { msgList, onEnter, clearMessages, modelType }
}
export default useChart
