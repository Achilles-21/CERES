<script lang="ts" setup>
import useSystemStore from '@/store/useSystemStore'

const { proxy }: { proxy: CusProxy } = getCurrentInstance();
const { query } = useRoute();
const data = ref({})
const valHtml = ref('')
const htmlId = ref('')
const getVideoInfo = async () => {
  const res = await proxy.$api.video.playVideo({ id: query.id });
  if (res.data) {
    res.data.videoPopRespList.forEach((item)=> item.questionTime= item.questionTime.split('~'))
  }
  data.value = res.data || {}
  console.log(data.value);

}
const getNoteInfo = async () => {
  const res = await proxy.$api.note.getNote({ videoId: query.id });
  if (res.code == 200) {
    valHtml.value = res.data.content
    htmlId.value = res.data.id
  } else {
    window.$msg.destroyAll()
  }
}
onMounted(() => {
  getVideoInfo()
  getNoteInfo()

})
onBeforeUnmount(() => {
  window.$modal.destroyAll()
})
</script>

<template>
  <XwyaLayout
    :videoId="query.id"
    :id="htmlId"
    :v="valHtml"
    :up="true"
    :disable="useSystemStore().userInfo.level == '1'"
    resizable
    hide-chat-header
    hide-assistant-description
    :chat-assistant-title="$t('chat.askAiTeacher')"
    :chat-assistant-role-label="$t('chat.virtualEyeTeacher')"
  >
      <template #default="f">
        <XwyaVideo  v-bind="f" :data="data"  :disable="useSystemStore().userInfo.level == '1'"/>
      </template>
  </XwyaLayout>
</template>
