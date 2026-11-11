<script lang="ts" setup>
import { getImageInfo,createImage,updateImage } from '@/api/image';
import { useImageAddForm } from '@/hooks/useQueryForm';
const { formData, addImageFormItem } = useImageAddForm();

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
    default: () =>{}
  }

})

const rules: NaiveUI.FormRules = computed(() => {
  return addImageFormItem.value.reduce((acc, cur, _) => {
    if (cur.path === "fileUrl") {
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
  const res = await getImageInfo({ id: props.id })
  res.data.fileUrl = [
  {
          id: res.fileId,
          name: res.data.name,
          status: 'finished',
          type: res.data.fileType,
          url:res.data.imageUrl
    },
  ]

  formData.value = res.data
}
const onClose = () => {
  props.modal.destroy()
}
const creayeImages = async () => {
  const res = await createImage({...formData.value,fileId:formData.value.fileUrl[0].id})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateImages = async () => {

  const res = await updateImage({...formData.value,fileId:formData.value.fileUrl[0].id})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await updateImages()
      } else {
        await creayeImages()
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addImageFormItem"
        v-model="formData">
        <template #default="{ validate, reset }">
          <div class="w-full flex justify-end gap-4">
            <n-button type="primary" @click="submit(validate)"> 提交</n-button>
            <n-button @click="onClose">取消</n-button>
          </div>

        </template>
      </XwyaForm>
</template>

<style lang="scss" scoped></style>
