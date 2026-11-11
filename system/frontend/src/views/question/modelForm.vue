<script lang="ts" setup>
import { createQuestion,updateQuestion,getQuestionInfo } from '@/api/question';
import { useQuestionAddFrom } from '@/hooks/useQueryForm';
const { formData, addQuestionFormItem } = useQuestionAddFrom();

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
    default: 0
  },
  getList: {
    type: Function,
    default: () =>{}
  }
})
const rules: NaiveUI.FormRules = computed(() => {
  return addQuestionFormItem.value.reduce((acc, cur, _) => {
    if(formData.value.type == '2') {
      if('ABCDEF'.indexOf(cur.label) == -1 && cur.path != 'answer' && cur.path != 'imageUrl') acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
      else acc[cur.path] = [{ required: false }]
    } else {
      if(cur.label != 'E' && cur.label != 'F' && cur.path != 'imageUrl')
      acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    }
    return acc
  }, {})
})

const getFormData = async () => {
  const res = await getQuestionInfo({ id: props.id })
  delete res.data.createTime
  if(res.data.type == '1')
  res.data.answer = res.data.answer.split(' ')
  res.data.imageUrl = res.data.imgId ? [
    {
      id: res.data.imgId,
      name: res.data.name,
      status: 'finished',
      type: res.data.fileType,
      url:res.data.imgUrl
    }
  ] : []
  formData.value = res.data

}
const onClose = () => {
  props.modal.destroy()
}
const createVideos = async () => {
  if(formData.value.imageUrl && formData.value.imageUrl.length > 0) {
    formData.value.imgId = formData.value.imageUrl[0].id
    formData.value.imgUrl = formData.value.imageUrl[0].url
  }
  delete formData.value.imageUrl
  const res = await createQuestion(formData.value)
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateVideos =  async() => {
  if(formData.value.imageUrl && formData.value.imageUrl.length > 0) {
    formData.value.imgId = formData.value.imageUrl[0].id
    formData.value.imgUrl = formData.value.imageUrl[0].url
  }
  delete formData.value.imageUrl
  delete formData.value.updateTime
  const res =await  updateQuestion(formData.value)
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  if(formData.value.type == '1')
  formData.value.answer = formData.value.answer.join(' ')
  validate()(async (errors) => {

    if (!errors) {
      if (props.flag) {
     await updateVideos()
    } else {
      await createVideos()
    }
    props.getList()
    } else {
      window.$msg.error("错误")
    }
  })
}
watch(() => formData.value.a10, (newVal) => {
  formData.value={ a10:newVal}
  });
onMounted(() => {
props.flag && getFormData()
})
</script>
<template>
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addQuestionFormItem"
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
