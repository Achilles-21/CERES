<script lang="ts" setup>
import { createTest,updateTest,getTestInfo } from '@/api/test';
import { useTestAddFrom } from '@/hooks/useTestForm';
const { formData, addTestFormItem } = useTestAddFrom();

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
  return addTestFormItem.value.reduce((acc, cur, _) => {
    if(formData.value.type == '0') {
      if(cur.label != 'E' && cur.label != 'F' && cur.path != 'imageUrl') {
        acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
      } else {
        acc[cur.path] = [{ required: false }]
      }
      return acc
    }
    if(formData.value.type == '2') {
      if(cur.path == "type" || cur.path == "content" || cur.path == "answerExplain" || cur.path == "description" || cur.path == "cateId") {
        acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
      } else {
        acc[cur.path] = [{ required: false }]
      }
      return acc
    }
  }, {})
})

const getFormData = async () => {
  const res = await getTestInfo({ id: props.id })
  delete res.data.createTime
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
  formData.value.type = formData.value.type ?? '0'
}
const onClose = () => {
  props.modal.destroy()
}
const createVideos = async () => {
  if(formData.value.imageUrl && formData.value.imageUrl.length > 0) {
    formData.value.imgId = formData.value.imageUrl[0].id
    formData.value.imgUrl = formData.value.imageUrl[0].url
  }
  const res = await createTest(formData.value)
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateVideos =  async() => {
  delete formData.value.updateTime
  if(formData.value.imageUrl && formData.value.imageUrl.length > 0) {
    formData.value.imgId = formData.value.imageUrl[0].id
    formData.value.imgUrl = formData.value.imageUrl[0].url
  }
  const res =await  updateTest(formData.value)
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
      window.$msg.error("错误")
    }
  })
}
onMounted(() => {
props.flag && getFormData()
})
</script>
<template>
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addTestFormItem"
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
