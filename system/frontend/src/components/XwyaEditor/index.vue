<script lang="ts" setup>
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { IToolbarConfig} from '@wangeditor/editor'
import '@/utils/menu.ts'
import i18n from "@/lang"

const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const props = defineProps(["videoId","v","up","id"])
const valHtml = ref(props.v?props.v:"")
const editorRef = shallowRef()
const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['blockquote', 'header1', 'header2', 'header3', "fullScreen", "insertVideo", "insertLink", "todo", "codeBlock"],
  insertKeys: {
    index: 10,
    keys: ["videoScreenshot"]
  }
}
let flag = true
const editorConfig = {
  placeholder: i18n.global.t('system.pleaseEnterContent'),
  MENU_CONF: {
    uploadImage: {
      customUpload: async (file, insertFn) => {
        const formData = new FormData()
  formData.append('folderName', 'image')
  formData.append('file', file as File)
      const res  = await fetch(import.meta.env.VITE_APP_URL+'/file/upload', {
        method: 'POST',
        headers: {
          'Authorization': localStorage.getItem('token'),
        },
        body: formData,
      })
        const json = await res.json()
        if (json.code === 200) {
          insertFn(json.data.fileUrl, '', json.data.fileUrl)
        }
      }
    }
  }
}
const handleCreated = (editor) => {
  editorRef.value = editor  
}
const save = async () => {

  const res = await proxy.$api.note.saveNote({ content: valHtml.value,id:props.id,videoId: Number(props.videoId) })
}
const updateNote = async (id:number) => {
  return  proxy.$api.note.editNote({ content: valHtml.value,  id,videoId:Number(props.videoId) })
}
defineExpose({
  updateNote
})
watchEffect(() => {
  if (props.v && flag) {

    valHtml.value = props.v
    flag=false
  }

})
onBeforeUnmount(() => {
  if ((valHtml.value && !props.v) || props.up) {
    save()
  }
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<template>
    <transition appear name="fade-transform" mode="out-in">
  <div class="w-full h-full  flex flex-col">
      <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRef" :defaultConfig="toolbarConfig" mode="simple" />
      <Editor class="flex-1" style="overflow-y: hidden;" v-model="valHtml" :defaultConfig="editorConfig" mode="simple"
        @onCreated="handleCreated" />
  </div>
</transition>

</template>

<style lang="css" scoped></style>
