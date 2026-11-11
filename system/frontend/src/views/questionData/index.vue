<script lang="ts" setup>
import  {NTooltip} from 'naive-ui'
import usePage from '@/hooks/usePage'
import Bntlist from '@/components/BntList'
import  XwyaButton from '@/components/XwyaButton'
import ViewModal from "./viewModal.vue"
const { page, total, loading, data } = usePage()
const pageDescription = '汇总用户习题完成情况，快速进入详情查看答题数据。'
const { proxy }: {proxy:CusProxy} = getCurrentInstance()
const initColumns = () => {
  return [
    {
      type: 'selection',
      fixed: 'left'
    },
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'userId'
    },
    {
      title: proxy.$t('userPage.name'),
      key: 'userName'
    },
    {
      title: proxy.$t('questionData.exerciseStatus'),
      key: 'answerCount'
    },
    {
      title: proxy.$t('pageGlobal.operation'),
      key: 'actions',
      render(row) {
        return h(NTooltip,{ trigger: 'hover'},{
          trigger: ()=> h(XwyaButton,{
            type: 'primary',
            icon: 'iconify gg--eye  text-xl text-white',
            onClick: ()=>showModel(row.userId)
          }),
          default: ()=> proxy.$t('pageGlobal.viewDetails')
        })
      }
    }]
}
const showModel =  (id) => {
   const modal= window.$modal.create({
    title:"查看详情",
    preset: 'card',
    style: {
      width: "600px",
      overflow: 'hidden'
    },
   content: () => h(ViewModal, {modal,id})
  })

}
const pagination = computed(() => (
  {
    itemCount: total.value,
    pageSizes: [10, 20],
    pageSlot: 7,
    page: page.pageNum,
    "onUpdate:page": (p: number) => {
      page.pageNum = p
      getList()
    },
  }
))

const getList = async () => {
  loading.value = true
  const res = await proxy.$api.question.getQueList(page)
  if (res.code === 200) {
    data.value = res.data.row
    total.value = res.data.total
  }

  loading.value= false
}
onMounted(() => {
  getList()
})
</script>

<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--sharp-troubleshoot text-base"></span>
          {{ $t('menu.analysis') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('menu.exercises') }}</h1>
          <p class="mt-2 max-w-3xl text-sm leading-6 text-slate-500">{{ pageDescription }}</p>
        </div>
      </div>
      <div class="ceres-toolbar flex flex-wrap items-center justify-start gap-2 lg:justify-end">
        <n-tooltip trigger="hover">
          <template #trigger>
            <XwyaButton class="ceres-pill cursor-pointer transition-all duration-200" secondary :loading="loading" icon="iconify ic--baseline-refresh text-lg" text="刷新" @click="getList" />
          </template>
          刷新列表
        </n-tooltip>
      </div>
    </div>
    <div class="overflow-x-auto">
      <XwyaTabel class="min-w-[760px]" :row-key="(r)=>r.userId" :columns="initColumns()" :data="data"   :pagination="pagination" :loading="loading" />
    </div>
  </section>
</template>

<style lang="scss" scoped></style>
