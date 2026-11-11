<script lang="ts" setup>
import { getVideoInfo,createVideo,updateVideo } from '@/api/video';
import { useVideoAddForm } from '@/hooks/useQueryForm';
import i18n from "@/lang"
const { formData, addVideoFormItem } = useVideoAddForm();
const props = defineProps({
  flag: {
    type: Boolean,
    default: false
  },
  modal: {
    type: Object,
    default: () => ({})
  },
  id: {
    type: Number,
    default:0
  },
  getList: {
    type: Function,
    default: () => {}
  }
})
const rules: NaiveUI.FormRules = computed(() => {
  return addVideoFormItem.value.reduce((acc, cur, _) => {
    if (cur.path === "fileUrl" || cur.path === "imageUrl" ||  cur.path === "pptUrl") {
      acc[cur.path] = [{
        required: true,
        validator: (rule, value) => {
        if (value && value.length >0) {
          return Promise.resolve();
        }
        return Promise.reject(cur.placeholder);
      }
      }
      ]
    } else {
      acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    }
    return acc
  }, {})
})

const getFormData = async () => {

  const res = await getVideoInfo({ id: props.id })
  res.data.fileUrl = [
  {
          id: res.data.fileId,
          name: res.data.name,
          status: 'finished',
          type: res.data.fileType,
          url:res.data.videoUrl
    },
  ]
  if (res.data.posterUrl) {
    res.data.imageUrl = [
  {
          id: res.data.posterId,
          name: res.data.name,
          status: 'finished',
          type: "image/*",
          url:res.data.posterUrl
    },
  ]
  }
  if (res.data.pptUrl) {
    res.data.pptUrl = [
      {
        id: new Date().getTime(),
        name: res.data.name,
        status: 'finished',
        type: ".ppt,.pptx",
        url: res.data.pptUrl
      },
    ]
  }
  res.data.isShow = res.data.isShow ? '1' : '0'
  res.data.duration = res.data.duration.toString()
  formData.value = res.data

}
const onClose = () => {
  props.modal.destroy()
 }
const createVideos = async () => {


   const res = await createVideo({ ...formData.value ,fileId: formData.value.fileUrl[0].id,posterId: formData.value.imageUrl[0].id,pptUrl:formData.value.pptUrl[0].url})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateVideos = async () => {


  const res =await  updateVideo({ ...formData.value ,fileId: formData.value.fileUrl[0].id,posterId: formData.value.imageUrl[0].id,pptUrl:formData.value.pptUrl[0].url})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await updateVideos()
      } else {
        await createVideos()
      }
      props.getList()
    } else {
      window.$msg.error("请填写表单")
    }
  })
}
onMounted(() => {
props.flag && getFormData()
})
</script>
<template>
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addVideoFormItem"
        v-model="formData">
        <template #default="{ validate, reset }">
          <div class="w-full flex justify-end gap-4">
            <n-button type="primary" @click="submit(validate)"> 提交</n-button>
            <n-button @click="onClose(reset)">取消</n-button>
          </div>

        </template>
      </XwyaForm>
</template>

<style lang="scss" scoped></style>
