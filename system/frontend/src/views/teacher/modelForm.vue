<script lang="ts" setup>
import { addTeacher,upTeacher,getTeacher } from '@/api/teacher';
import { useAddTeacherForm } from '@/hooks/useQueryForm';
const { formData, addTeacherFormItem } = useAddTeacherForm();
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
  return addTeacherFormItem.value.reduce((acc, cur, _) => {
    if (cur.path === "teacherImage") {
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
  const res = await getTeacher(props.id)
  res.data.teacherImage = [
  {
          id: Date.now(),
          name: res.data.name,
          status: 'finished',
    type: 'image/gif'   ,
          url:res.data.teacherImage
    },
  ]
  res.data.imageTime= res.data.imageTime.toString()
  formData.value = res.data
}
const onClose = () => {
  props.modal.destroy()
 }
const addTeachers = async () => {
   const res = await addTeacher({...formData.value,teacherImage:formData.value.teacherImage[0].url,videoId:props.videoId})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const upTeachers = async () => {
  const res =await  upTeacher({...formData.value,teacherImage:formData.value.teacherImage[0].url,videoId:props.videoId})
      if (res.code === 200) {
        props.modal.destroy()
      }
}
const submit = (validate: any) => {
  validate()(async (errors) => {
    if (!errors) {
      if (props.flag) {
        await upTeachers()
      } else {
        await addTeachers()
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
    <XwyaForm  :rules="rules" label-width="auto" label-placement="left" :item-list="addTeacherFormItem"
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
