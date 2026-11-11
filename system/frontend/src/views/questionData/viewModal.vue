<script lang="ts" setup>
import { getQueDtails } from '@/api/question';
const props = defineProps(["modal", "id"])
const list = ref([])
const getDetail = async () => {
  const res = await getQueDtails(props.id)
  list.value = res.data
}
onMounted(() => {
  getDetail()
})
</script>

<template>
  <div class=" h-[600px] overflow-auto ">
    <XwyaSubjectType :data="p" :index="i" :showLike="false"  v-for="(p, i) in list" :key="p.queId" />
  </div>
</template>

<style lang="scss" scoped></style>
