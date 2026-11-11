<script lang="ts" setup>
import usePage from '@/hooks/usePage'
import i18n from '@/lang'
const size = (document.documentElement.scrollHeight - 65) / 240;
const { page, total, loading, data } = usePage(parseInt(size) * 4)
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { push } = useRouter()
const boxStyle = {
  boxShadow: "0.3em 0.3em 0.7em #00000015",
  border: "rgb(250, 250, 250) 0.2em solid"
}
const pageConfig = computed(() => ({
  itemCount: total.value,
  pageSizes: [10, 20],
  pageSlot: 7,
  pageSize: page.pageSize,
  page: page.pageNum,
  "onUpdate:page": (p: number) => {
    page.pageNum = p
    getList()
  },
}))
const getList = async () => {
  const res = await proxy.$api.test.getFrontTestCateList()
  data.value = res.data

}
const pushPage = async (id) => {
  push(`/test?cateId=${id}`)
}
onMounted(() => {
  getList()
})
</script>

<template>
  <div class="font-cjk-page w-full h-full">
    <div class="max-w-[1024px] py-4 w-full m-auto flex flex-wrap gap-4 ">
      <div @click="pushPage(p.id)"
        class="w-[calc(25%-16px)] h-[240px]  bg-white rounded-xl overflow-hidden hover:scale-105 relative" :style="boxStyle"
        v-for="p in data" :key="p.id">
        <img class=" h-36 w-full"
          :src="p.imageUrl"
          alt="">
        <div class="px-1 py-2  ">
          <p class="course-title">{{ p.name }}</p>
          <div class="flex justify-end text-gray-300">
            <span class="iconify gg--eye-alt text-xl"></span>
            <span class="px-1">{{ p.hits?p.hits:0 }}</span>
          </div>
        </div>
      </div>
      <div class=" py-4 w-full flex justify-center ">
        <n-pagination v-bind="pageConfig" />
      </div>
    </div>

  </div>
</template>

<style lang="css" scoped>
.course-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
