<script lang="ts" setup>
import { createTestCate,updateTestCate, getTestIdList } from '@/api/test';
import { useTestCateAddFrom } from '@/hooks/useTestForm';
const { formData, addTestCateFormItem } = useTestCateAddFrom();

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
  },
  formData: {
    type: Object,
    default: () => ({})
  }
})
const rules: NaiveUI.FormRules = computed(() => {
  return addTestCateFormItem.value.reduce((acc, cur, _) => {
    if(cur.path == 'imageUrl' || cur.path == 'queIds') acc[cur.path] = [{ required: false }]
    else if(cur.path == 'limitTime') acc[cur.path] = [{ required: true }]
    else acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    return acc
  }, {})
})

const getFormData = async () => {
  const data = Object.assign({},props.formData)
  data.auth = data.auth ? data.auth + '' : '0'
  delete data.createTime
  data.imageUrl = [
  {
          id: data.imageId,
          name: data.name,
          status: 'finished',
          type: data.fileType,
          url:data.imageUrl
    },
  ]
  const res = await getTestIdList(props.id)
  res.data.forEach((element, index) => {
    res.data[index] = element + ''
  });
  data.queIds = res.data
  formData.value = data
}
const onClose = () => {
  props.modal.destroy()
}
const createVideos = async () => {
  formData.value.imageId = formData.value.imageUrl && formData.value.imageUrl.length ? formData.value.imageUrl[0].id : ''
  const res = await createTestCate(formData.value)
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateVideos =  async() => {
  delete formData.value.updateTime
  formData.value.imageId = formData.value.imageUrl[0].id
  const res =await  updateTestCate(formData.value)
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addTestCateFormItem"
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
