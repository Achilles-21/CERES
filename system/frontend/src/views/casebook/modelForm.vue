<script lang="ts" setup>
import { getCasebook,addCasebook,upCasebook} from "@/api/casebook"
import { useAddCasebook } from '@/hooks/useQueryForm';
const { formData, addCasebookFormItem } = useAddCasebook();

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
  return addCasebookFormItem.value.reduce((acc, cur, _) => {
    if (cur.path.includes('question') || cur.path.includes('answer')) return acc
    if (cur.path === "casePicsList") {
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
  const res = await getCasebook( props.id)
        res.data.casePicsList  = res.data.casePicsList.map(item=>({
          id: Date.now(),
          name: `${Date.now()}.png`,
          status: 'finished',
          type: "image/*",
          url:item.pic
        }))
  res.data.patientImage = res.data.patientImage.toString()
  res.data.questionsNumber = res.data.questionsNumber.toString()
  formData.value = res.data

}
const onClose = () => {
  props.modal.destroy()
}
const creayeCasebook = async () => {
  const casePicsList = formData.value.casePicsList.map((item: any) => item.url)
  const res = await addCasebook({ ...formData.value, casePicsList, questions_number: Number(formData.value.questionsNumber) })
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const updateCasebook = async () => {
  const casePicsList = formData.value.casePicsList.map((item: any) => item.url)
  const res = await upCasebook({...formData.value,casePicsList,questions_number: Number(formData.value.questionsNumber)})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await updateCasebook()
      } else {
        await creayeCasebook()
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
  <div class=" h-[400px] overflow-auto">
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addCasebookFormItem"
        v-model="formData">
        <template #default="{ validate, reset }">
          <div class="w-full flex justify-end gap-4">
            <n-button type="primary" @click="submit(validate)"> 提交</n-button>
            <n-button @click="onClose">取消</n-button>
          </div>

        </template>
      </XwyaForm>
  </div>

</template>

<style lang="scss" scoped></style>
