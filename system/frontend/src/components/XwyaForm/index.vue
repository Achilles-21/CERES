<template>
  <n-form style="width: 100%;" :inline="false" ref="formRef" :model="modelValue" :disabled="disabled" v-bind="$attrs">
    <n-flex wrap >
      <component  v-for="(p, i) in itemList" :key="i"  :is="type[(p as ComponentMap).type]" :value="modelValue"
        :style="{ width:p.itemwidth?p.itemwidth:`calc(${100 / row }% - 10px)` }" v-bind="p" ></component>
      <transition>
        <template v-for="(_, name) in $slots" #[name]="scope">
          <slot :name="name"  :key="name" :change="onChange" :state="disabled" :validate="validate" :reset="reset" v-bind="scope"></slot>
        </template>
      </transition>
    </n-flex>
  </n-form>
</template>

<script lang="ts" setup>
import MyInput from "./MyInput"
import MyCheck from "./MyCheck"
import MySelect from "./MySelect"
import MyDate from "./MyDete";
import MyRadio from "./MyRadio";
import MyButton from "./MyButton"
import MyTags from "./MyTags"
import MyNumberInput from './MyNumberInput'
import MySwitch from "./MySwitch"
import MyCascader from './MyCascader'
import MyTransfer from './MyTransfer'
import MyAutoInput from "./MyAutoInput/"
import MyUpload from './MyUpload'
interface ComponentMap {
  input: ComponentType;
  check: ComponentType;
  select: ComponentType;
  date: ComponentType;
  radio: ComponentType;
  tags: ComponentType;
  number: ComponentType;
  switch: ComponentType;
  cascader: ComponentType;
  transfer: ComponentType;
  auto: ComponentType;
  upload: ComponentType;
}
const type: ComponentMap = {
  input: MyInput,
  check: MyCheck,
  select: MySelect,
  date: MyDate,
  radio: MyRadio,
  tags: MyTags,
  number: MyNumberInput,
  switch: MySwitch,
  cascader: MyCascader,
  transfer: MyTransfer,
  auto: MyAutoInput,
  upload:MyUpload
}

const disabled = ref<boolean>(false)
defineProps({
  modelValue: {
    type: Object,
    default: void 0,
    required:true
  },
  itemList: {
    type: Array,
    default: () => [],
    required:true
  },
  row: {
    type: Number,
    default: 1,

  },
  inline: {
    type: Boolean,
    default: false
  }

})
const formRef = ref<NaiveUI.FormInst|null>(null)

const onChange = () => {
  disabled.value = !disabled.value
}
const validate = () => {
  return formRef.value.validate
}
const reset = () => {
  formRef.value.restoreValidation()
}
defineExpose({
  validate,
  disabled
})



</script>

<style scoped>

</style>
