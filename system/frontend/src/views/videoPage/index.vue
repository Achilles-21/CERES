<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-folder text-base"></span>
          {{ $t('menu.data') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('menu.video') }}</h1>
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
    <div class="ceres-card p-4">
      <div class="ceres-toolbar flex flex-col gap-3 lg:flex-row lg:items-center">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-filter-alt text-base"></span>
          筛选
        </div>
        <XwyaForm class="w-full flex-1" label-placement="left" :item-list="queryVideoListFormItem" v-model="formData" :row="7">
          <template #default="{ change, state }">
            <n-button class="ceres-pill cursor-pointer transition-all duration-200" :type="!state ? 'primary' : 'error'" @click="search(state, change)">
              {{ state ? $t(`pageGlobal.cancel`) : $t(`pageGlobal.search`) }}
            </n-button>
          </template>
        </XwyaForm>
      </div>
    </div>
    <div class="overflow-x-auto">
      <XwyaTabel class="min-w-[1120px]" :columns="initColumns()" :data="data"   :pagination="pageination" :loading="loading" />
    </div>
  </section>
</template>
<script lang="ts" setup >
import usePage from '@/hooks/usePage'
import Bntlist from '@/components/BntList'
import AddForm from './modelForm.vue'
import { NImage } from 'naive-ui'
import { useQueryVideoListForm } from '@/hooks/useQueryForm';
const { page, total, loading, data,reset } = usePage()
const pageDescription = '管理课程视频、封面、时长与问答入口，支持按课程名称快速筛选资源。'
const {formData,queryVideoListFormItem} = useQueryVideoListForm()
const {proxy} = getCurrentInstance()

const initColumns = () => {
  return [
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'id'
    },
    {
      width: 300,
      ellipsis:true,
      title: proxy.$t('video.courseName'),
      key: 'name'
    },
    {
      title: proxy.$t('video.videoCover'),
      key: 'imageUrl',
      render(row) {
        return row.imageUrl ? h(NImage,{src:row.imageUrl,width:96,height:64,objectFit:'cover',style:{borderRadius:'10px'}}) : '-'
      }
    },
    {
      width: 300,
      ellipsis:true,
      title:  proxy.$t('video.url'),
      key: 'videoUrl'
    },
    {
      width: 300,
      ellipsis:true,
      title: proxy.$t('pageGlobal.describe'),
      key: 'description'
    },
    {
      title: proxy.$t('video.duration'),
      key: 'duration'
    },
    {
      title: proxy.$t('pageGlobal.operation'),
      key: 'actions',
      render(row) {
        return h(Bntlist,{onEdit:()=>showModel(proxy.$t(`pageGlobal.edit`),true,row.id),onDelete:()=>onDelete(row),teacher:true,id:row.id})
      }
    }]
}

const onDelete = async (row:any) => {
  const res = await proxy.$api.video.deleteVideo({ id: row.id })
  if (res.code == 200) {
    getList()
  }
}

const showModel = (title: string,flag:boolean,id:number=0) => {
 const modal= window.$modal.create({
    title,
    preset: 'card',
    style: {
      width:"600px"
    },
   content: () => h(AddForm, {flag,modal,id,getList})
  })
}

const pageination = computed(() => (
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
  const res = await proxy.$api.video.getVideoList({...page,...formData.value})
  if (res.code === 200) {
    data.value = res?.data?.row
    total.value = res?.data?.total
  }
  loading.value = false
}
const search = (state, change) => {
  reset()
  if (state) {
    formData.value = {name:""}
  }
  getList()
  change()
}
onMounted(() => {
  getList()
})
</script>
<style lang="scss" scoped></style>
