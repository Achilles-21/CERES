<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-video-camera-back text-base"></span>
          {{ $t('menu.video') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('pageGlobal.teacher') }}</h1>
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
      <XwyaTabel class="min-w-[760px]" :columns="initColumns()" :data="data"   :pagination="pageination" :loading="loading" />
    </div>
  </section>
</template>
<script lang="ts" setup >
import usePage from '@/hooks/usePage'
import Bntlist from '@/components/BntList'
import AddForm from './modelForm.vue'
import { NImage } from 'naive-ui';
const { page, total, loading, data,reset } = usePage()
const pageDescription = '维护当前课程的视频节点与教师形象资源，让课程播放时的讲解状态更稳定。'
const { proxy } = getCurrentInstance()
const {query} = useRoute()


const initColumns = () => {
  return [
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'id'
    },
    {
      width: 300,
      ellipsis:true,
      title: proxy.$t('teacher.videoNode'),
      key: 'imageTime'
    },
    {
      width: 300,
      ellipsis:true,
      title:  proxy.$t('teacher.teacherImage'),
      key: 'teacherImage',
      render(row) {
        return h(NImage,{src:row.teacherImage,width:100,height:100,objectFit:'cover',style:{borderRadius:'10px'}})
      }
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
  const res = await proxy.$api.teacher.deleteTeacher(row.id)
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
   content: () => h(AddForm, {flag,modal,id,getList,videoId:query.id}),
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
  const res = await proxy.$api.teacher.getTeacherList(query.id)
  if (res.code === 200) {
    data.value = res?.data

  }
  loading.value = false
}

onMounted(() => {
  getList()
})
</script>
<style lang="scss" scoped></style>
