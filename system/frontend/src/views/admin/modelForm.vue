<script lang="ts" setup>
import { useAddAdminForm } from '@/hooks/useQueryForm';
import { addUser,editUser } from '@/api/user';
const {     formData,adminAddFormItem } = useAddAdminForm();

const props = defineProps({
  flag: {
    type: Boolean,
    default: false
  },
  modal: {
    type: Object,
    default: () => ({})
  },
  info: {
    type: Object,
  },
  getList: {
    type: Function,
    default: () =>{}
  }
})
const rules: NaiveUI.FormRules = computed(() => {
  return adminAddFormItem.value.reduce((acc, cur, _) => {

    if(cur.path==="password" && props.flag) return acc
    acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    return acc
  }, {})
})

const getFormData = async () => {
  formData.value = {...props.info}
  formData.value.level = formData.value.level ? formData.value.level : '0'
}
const onClose = () => {
  props.modal.destroy()
}
const addUsers = async () => {

  const res = await addUser({...formData.value,nickName:formData.value.username})
  if (res.code === 200) {
        props.modal.destroy()
      }
}
const editUsers = async () => {
  const res = await editUser({...formData.value,nickName:formData.value.username})
  if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await editUsers()

      } else {
        await addUsers()

      }
      props.getList()
    } else {
      window.$msg.error("请填写表单")
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="adminAddFormItem"
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
