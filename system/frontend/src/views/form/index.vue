<template>
  <div class="p-4">
    <div class="border-0 border-l-4 border-solid border-green-400 text-2xl font-bold pl-4 ">查询示例</div>
    <XwyaForm class="m-10" label-placement="left" :item-list="queryFormItem" v-model="queryFormData" :row="7">
      <template #default="{ change, state }">
        <n-button :type="!state ? 'primary' : 'error'" @click="search(state, change)">
          {{ state ? '取消' : '搜索' }}
        </n-button>
      </template>
    </XwyaForm>
    <div class="border-0 border-l-4 border-solid border-green-400 text-2xl font-bold pl-4 ">正常表单示例</div>
    <div class="m-10 w-[700px]">
      <XwyaForm :rules="rules" label-width="auto" label-placement="left" :item-list="queryFormItem2"
        v-model="queryFormData" :row="3">
        <template #default="{ validate, reset }">
          <div class="w-full flex justify-center gap-4">
            <n-button type="primary" @click="submit(validate)"> 提交</n-button>
            <n-button @click="onReset(reset)">重置</n-button>
          </div>

        </template>
      </XwyaForm>
    </div>
    <div class="border-0 border-l-4 border-solid border-green-400 text-2xl font-bold pl-4 ">动态表单示例</div>
    <div class="m-10 w-[700px]">
      <XwyaForm :rules="rules" label-width="auto" label-placement="left" :item-list="queryFormItem3"
        v-model="queryFormData" :row="3">
        <template #default="{ validate, reset }">
          <div class="w-full flex justify-center gap-4">
            <n-button type="primary" @click="submit(validate)"> 提交</n-button>
            <n-button @click="onReset(reset)">重置</n-button>
          </div>

        </template>
      </XwyaForm>
    </div>
  </div>
</template>

<script setup lang="ts">
class QueryFormData {
  remarks = ''
  address = ""
  remarks221214222 = ""
  a1 = ""
}

const queryFormItem = [
  { label: '地址:', path: 'address111', type: 'input', placeholder: '请输入地址' },
  { label: '备注:', path: 'remarks222', type: 'select', placeholder: '请输入备注', options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }] },
  { label: '备注:', path: 'remarks1', type: 'input', placeholder: '请输入备注' },
  { label: '备注:', path: 'remarks2', type: 'input', placeholder: '请输入备注' },
]
const queryFormData = ref<QueryFormData>(new QueryFormData())
const oo = computed(() => {
  return ['@gmail.com', '@163.com', '@qq.com'].map((suffix) => {
    const prefix = queryFormData.value.remarks221214222.split('@')[0]
    return {
      label: prefix + suffix,
      value: prefix + suffix
    }
  })
})
// Wrap autocomplete options in a ref so input changes remain reactive.
const queryFormItem2 = ref([
  { label: 'input输入框:', path: 'a1', itemwidth: '100%', type: 'input', placeholder: '请输入地址' },
  { label: 'textarea文本框:', path: 'a2', itemwidth: '100%', type: 'input', nativeType: "textarea", placeholder: '请输入地址' },
  { label: 'auto输入框:', path: 'a3', type: 'auto', itemwidth: "100%", options: oo.value },
  { label: 'select多选:', path: 'a4', itemwidth: '100%', type: 'select', multiple: true, placeholder: '请输入备注', options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }, { label: "选项2", value: 3 }, { label: "选项4", value: 4 }] },
  { label: 'select单选:', path: 'a5', type: 'select', itemwidth: '100%', placeholder: '请输入备注', options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }] },
  { label: 'date日期:', path: 'a6', type: 'date', itemwidth: '100%', placeholder: '请输入备注' },
  { label: 'datetime日期加时间:', path: 'a7', type: 'date', nativeType: "datetime", itemwidth: '100%', placeholder: '请输入备注' },
  { label: 'daterange日期范围:', path: 'a8', type: 'date', nativeType: "daterange", itemwidth: '100%', placeholder: '请输入备注' },
  { label: 'check多选复选框:', path: 'a9', type: 'check', placeholder: '请输入备注', options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }], itemwidth: "100%" },
  { label: 'radio单选按钮:', path: 'a10', type: 'radio', itemwidth: "100%", options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }] },
  { label: 'tags标签:', path: 'a11', type: 'tags', itemwidth: "100%" },
  { label: 'number计数器:', path: 'a12', type: 'number', itemwidth: "100%" },
  { label: 'switch开关:', path: 'a13', type: 'switch', itemwidth: "100%" },
  { label: 'cascader级联选择器:', path: 'a14', type: 'cascader', itemwidth: "100%", options: [{ label: "选项1", value: 1, children: [{ label: "选项11", value: 11 }] }, { label: "选项2", value: 2, children: [{ label: "选项22", value: 22 }] }] },
  { label: 'transfer穿梭框:', path: 'a15', type: 'transfer', itemwidth: "100%", options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }] },
  { label: '图片上传:', path: 'a16', type: 'upload', itemwidth: "100%", data: { name: 'xwya', }, headers: { 'Authorization': 'Bearer ' }, withCredentials: true, url: "/apii/common/upload" },
])
const rules: NaiveUI.FormRules = computed(() => {
  return queryFormItem2.value.reduce((acc, cur, _) => {
    acc[cur.path] = [{ required: true, trigger: ['blur'], }]
    return acc
  }, {})
})

const queryFormItem3 = computed(() => {
  return [
    { label: '动态input:', path: 'b1', itemwidth: '100%', type: 'input', placeholder: '请输入123' },
    { label: '123显示下拉:', path: 'b2', itemwidth: '100%', type: 'select', placeholder: '请选择选项2',options: [{ label: "选项1", value: 1 }, { label: "选项2", value: 2 }, { label: "选项2", value: 3 }, { label: "选项4", value: 4 }] ,isShow: queryFormData.value.b1=='123' },
    { label: 'abc:', path: 'b3', itemwidth: '100%', type: 'input', placeholder: '请输入456', isShow: queryFormData.value.b2 == 2 },
    { label: 'datetime日期加时间:', path: 'a7', type: 'date', nativeType: "datetime", itemwidth: '100%', placeholder: '请输入备注',isShow: queryFormData.value.b3 == '456' },
  ]
})
const submit = (validate) => {
  console.log(queryFormData.value);
  validate()(err => {
    if (!err) {
      window.$msg.success('校验通过')
    } else {
      window.$msg.error('失败')
    }
  })
}
const onReset = (f: Function) => {
  queryFormData.value = new QueryFormData()
  f()
}
const search = (state, change) => {
  if (state) {
    window.$msg.success('搜索')
  } else {
    queryFormData.value = new QueryFormData()
    window.$msg.success('取消搜索')
  }
  change()
}


</script>

<style lang="css" scoped></style>
