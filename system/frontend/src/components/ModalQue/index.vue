<script lang="ts" setup>
import { sendVideoAnswer } from '@/api/question';
import i18n from '@/lang'
const props = defineProps(["modal", "data","play"])
const value = ref(null)
const onSubmit = async () => {
  if (!value.value) {

    return
  }
  const data = {
    id: props.data.id,
    content: props.data.content,
    answer: value.value
  }
  const res = await sendVideoAnswer(data)
  if (res.code === 200) {
    props.play()
    props.modal.destroy()
  }

}
const close = () => {
  props.modal.destroy()
}
</script>

<template>
  <div class="">
    <p>{{ data.content}}</p>
    <n-radio-group v-model:value="value" name="radiogroup" class="mt-4">
    <n-space vertical :size="10">
      <n-radio v-for="(p) in data?.options" :key="p.title" :value="p.choice">
        {{ p.choice }}
      </n-radio>
    </n-space>
  </n-radio-group>
  <div class="flex justify-end gap-4 mt-2">
    <n-button @click="close">{{ i18n.global.t('system.cancel') }}</n-button>
    <n-button type="success" @click="onSubmit">{{ i18n.global.t('system.confirm') }}</n-button>
  </div>
  </div>
</template>

<style lang="scss" scoped></style>
