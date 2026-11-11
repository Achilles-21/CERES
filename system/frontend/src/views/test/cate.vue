<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-folder text-base"></span>
          {{ $t('menu.data') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('menu.testCate') }}</h1>
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
      <XwyaTabel class="min-w-[720px]" :columns="initColumns()" :data="data" :loading="loading" />
    </div>
  </section>
</template>
<script lang="ts" setup >
import usePage from '@/hooks/usePage'
import Bntlist from '@/components/BntList'
import { NImage } from 'naive-ui'
import AddForm from './cateForm.vue'

const { page, total, loading, data } = usePage()
const pageDescription = '维护试卷类型与封面素材，保证考试入口分类清晰、可识别。'
const {proxy} = getCurrentInstance()

const initColumns = () => {
  return [
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'id'
    },
    {
      title: proxy.$t('image.image'),
      key: 'imageUrl',
      render(row) {
        return h(NImage,{src:row.imageUrl,width:100,height:100,objectFit:'cover',style:{borderRadius:'10px'}})
      }
    },
    {
      title: proxy.$t('menu.testCate'),
      key: 'name'
    },
    {
      title: proxy.$t('pageGlobal.operation'),
      key: 'actions',
      render(row) {
        return h(Bntlist,{onEdit:()=>showModel(proxy.$t(`pageGlobal.edit`),true,row),onDelete:()=>onDelete(row)})
      }
    }]
}

const onDelete = async (row:any) => {
  const res = await proxy.$api.test.deleteTestCate({ id: row.id })
  if (res.code == 200) {
    getList()
  }
}

const showModel = (title: string,flag:boolean,row) => {
 const modal= window.$modal.create({
    title,
   preset: 'card',
   maskClosable:false,
    style: {
      width:"600px"
   },
   content: () => h(AddForm, { flag,modal,id:row?.id,getList,formData:row})
  })
}



const getList = async () => {
  loading.value = true
  const res = await proxy.$api.test.getTestCateList()
  data.value = res.data
  loading.value = false

}
onMounted(() => {
  getList()
})
</script>
<style lang="scss" scoped></style>
