<script lang="ts" setup>
import { useAddUserForm } from '@/hooks/useQueryForm';
import { addUser,editUser } from '@/api/user';
const {     formData,userAddFormItem } = useAddUserForm ();
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

  return userAddFormItem.value.reduce((acc, cur, _) => {
    if(cur.path==="password" && props.flag) return acc
    acc[cur.path] = [{ required: true, trigger: [],message:cur.placeholder }]
    return acc
  }, {})
})

const getFormData = async () => {
  console.log("发送请求");

}

const onClose = () => {
  props.modal.destroy()
 }
const submit = (validate: any) => {
  console.log(formData.value);

  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await addUsers()
      } else {
        await editUsers()
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="userAddFormItem"
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
