<script lang="ts" setup>
import { updateTeacherAvatar } from "@/api/system";
import i18n from '@/lang'
const props = defineProps(["teacherPicture","initInfo","close"])
import t1 from "@/assets/image/t1.svg"
import t2 from "@/assets/image/t2.svg"
import t3 from "@/assets/image/t3.svg"
import t4 from "@/assets/image/t4.svg"

const avatarList = [
  { label: t2, value: "2" },
  { label: t3, value: "3" },
  { label: t4, value: "4" },
  {label:t1,value:"1"},
]
const value = ref(props.teacherPicture)

const confirm = async () => {
  const res = await updateTeacherAvatar({ teacherPicture: value.value })
  if (res.code === 200) {
    props.initInfo()
    props.close()
  }

}
</script>

<template>
  <div class="">
    <n-radio-group v-model:value="value" name="radiogroup">
    <n-space>
      <n-radio v-for="p in avatarList" :key="p.value" :value="p.value">
        <img :src="p.label" class="w-[80px] h-[80px]" alt="">
      </n-radio>
    </n-space>
  </n-radio-group>
  <div class="flex justify-end gap-4 mt-4">
    <n-button @click="close">{{ i18n.global.t('system.cancel') }}</n-button>
    <n-button type="success" @click="confirm">{{ i18n.global.t('system.confirm') }}</n-button>
  </div>
  </div>
</template>

<style lang="scss" scoped></style>
