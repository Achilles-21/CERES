<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-folder text-base"></span>
          {{ $t('menu.data') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('menu.casebook') }}</h1>
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
        <n-tooltip trigger="hover">
          <template #trigger>
            <XwyaButton class="ceres-pill cursor-pointer transition-all duration-200" @click="showModel($t(`pageGlobal.add`),false)" type="primary" icon="iconify gg--add text-lg text-white" :text="$t(`pageGlobal.add`)" />
          </template>
          {{ $t(`pageGlobal.add`) }}
        </n-tooltip>
      </div>
    </div>
    <div class="overflow-x-auto">
      <XwyaTabel class="min-w-[720px]" :columns="initColumns()" :data="data"   :pagination="pageination" :loading="loading" />
    </div>
  </section>
</template>
<script lang="ts" setup >
import usePage from '@/hooks/usePage'
import Bntlist from '@/components/BntList'
import AddForm from './modelForm.vue'
import { NImage } from 'naive-ui';
const {proxy}:{proxy:CusProxy} = getCurrentInstance() as any
const { page, total, loading, data } = usePage()
const pageDescription = '管理病例类型与案例资源，支撑病例分析流程中的题目与患者信息配置。'

const initColumns = () => {
  return [
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'id'
    },
    {
      width: 300,
      ellipsis:true,
      title: proxy.$t('casebook.caseName'),
      key: 'name'
    },
    {
      title: proxy.$t('pageGlobal.operation'),
      key: 'actions',
      render(row) {
        return h(Bntlist,{onEdit:()=>showModel(proxy.$t(`pageGlobal.edit`),true,row.id),onDelete:()=>onDelete(row)})
      }
    }]
}

const onDelete = async (row:any) => {
  const res = await proxy.$api.casebook.delCasebook(row.id)
  if (res.code == 200) {
    getList()
  }
}

const showModel = (title: string,flag:boolean,id:number=0) => {
  const modal = window.$modal.create({
    title,
    preset: 'card',
    style: {
      width:"600px"
    },
    content:()=>h(AddForm,{flag,modal,id,getList})
  })
}
const pageination = computed(() => (
  {
    itemCount: total.value,
    pageSizes: [10, 20],
    pageSlot: 7,
    page: page.pageNum,
    "onUpdate:page": (p) => {
      page.pageNum = p
      getList()
    },
  }
))

const getList = async () => {
  loading.value = true

  const res = await proxy.$api.casebook.getCasebookList(page)
  if (res.code === 200) {

    data.value = res.data.row?res.data.row:[]
    total.value = res.data.total ? res.data.total : 0

  }

    loading.value = false
}
onMounted(() => {
  getList()
})
</script>
<style lang="scss" scoped></style>
