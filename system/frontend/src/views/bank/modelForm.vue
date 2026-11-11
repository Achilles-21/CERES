<script lang="ts" setup>
import { getBankInfo,addBank,upBank } from '@/api/bank';
import { useAddBankForm } from '@/hooks/useQueryForm';
const { formData, addBankFormItem } = useAddBankForm();
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
  },
  videoId: {
    type: String,
    default:0
  },

})
const rules: NaiveUI.FormRules = computed(() => {
  return addBankFormItem.value.reduce((acc, cur, _) => {
    if(cur.path == 'isShow' || cur.path == 'popVideoId') {
      acc[cur.path] = [{ required: false }]
    } else {
      acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    }
    return acc
  }, {})
})

const getFormData = async () => {
  const res = await getBankInfo({id:props.id})
  res.data.queId = typeof res.data.queId == 'string' ? res.data.queId.split(' ') : res.data.queId
  res.data.popVideoId = res.data.popVideoId ? res.data.popVideoId.toString() : ''
  formData.value = res.data
}
const onClose = () => {
  props.modal.destroy()
 }
const addBanks = async () => {
  const arr = formData.value.questionTime.split('~')
    if (arr.length != 2) {
      window.$msg.error(i18n.global.t('video.time'))
      return
    }
   const res = await addBank({...formData.value,videoId:props.videoId})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const upBanks = async () => {
  const arr = formData.value.questionTime.split('~')
    if (arr.length != 2) {
      window.$msg.error(i18n.global.t('video.time'))
      return
    }
  const res =await  upBank({...formData.value,videoId:props.videoId})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  formData.value.queId = formData.value.queId.join(" ")
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await upBanks()
      } else {
        await addBanks()
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addBankFormItem"
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
