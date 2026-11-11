<template>
  <section class="ceres-page-shell admin-data-page space-y-4">
    <div class="ceres-card flex flex-col gap-4 p-5 lg:flex-row lg:items-start lg:justify-between">
      <div class="min-w-0 space-y-2">
        <div class="ceres-pill inline-flex items-center gap-2 text-sm">
          <span class="iconify ic--baseline-folder-shared text-base"></span>
          {{ $t('login.admin') }}
        </div>
        <div>
          <h1 class="text-2xl font-semibold leading-tight text-slate-950">{{ $t('menu.userAdmin') }}</h1>
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
        <XwyaForm class="w-full flex-1" label-placement="left" :item-list="adminQueryFormItem" v-model="queryFormData" :row="5">
          <template #default="{ change, state }">
            <n-button class="ceres-pill cursor-pointer transition-all duration-200" :type="!state ? 'primary' : 'error'" @click="search(state, change)">
              {{ state ? $t(`pageGlobal.cancel`) : $t(`pageGlobal.search`) }}
            </n-button>
          </template>
        </XwyaForm>
      </div>
    </div>
    <div class="overflow-x-auto">
      <XwyaTabel class="min-w-[820px]" :columns="initColumns()" :data="data" :row-key="(u) => u.userId" :pagination="initPagination" :loading="loading" />
    </div>
  </section>
</template>
<script lang="ts" setup>
import usePage from '@/hooks/usePage'
import { adminUserQueryForm } from '@/hooks/useQueryForm'
import Bntlist from '@/components/BntList'
import AddForm from './modelForm.vue'

const { page, total, loading, data } = usePage()
const pageDescription = '维护后台账号、角色与登录信息，确保管理端权限和用户资料可追踪。'
const { queryFormData, adminQueryFormItem } = adminUserQueryForm()
const { proxy }: { proxy: CusProxy } = getCurrentInstance()
const info = ref({})
const search = (state, change) => {
  change()
  page.pageNum=1
  if (state) {
  queryFormData.value = {username:""}

  }
  getList()
}
const initColumns = () => {
  return [
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'userId'
    },
    {
      title: proxy.$t('userPage.name'),
      key: 'username'
    },
    {
      title: proxy.$t('login.account'),
      key: 'phonenumber'
    },
    {
      title:proxy.$t('userPage.role'),
      key: 'roleName'
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
  const res = await proxy.$api.user.deleteUser(row)
  if (res.code == 200) {
    getList()
  }
}

const showModel = (title: string, flag: boolean, info: any = null) => {
  if (info != null) {
    info.roleId=info.roleName==="前台用户"?"3":"2"
  }
 const modal= window.$modal.create({
    title,
   preset: 'card',
   maskClosable:false,
    style: {
      width:"600px"
   },
   content: () => h(AddForm, {flag,modal,info, getList})
  })
}
const initPagination = computed(()=>({

    itemCount: total.value,
    pageSizes: [10, 20],
    pageSlot: 7,
    page: page.pageNum,
    "onUpdate:page": (p: number) => {
      page.pageNum = p
      getList()
  }
}))

const getList = async () => {
  loading.value = true
  const res = await proxy.$api.user.getUserList({...page,...queryFormData.value})
  if (res.code == 200) {
    data.value = res?.rows
    total.value = res?.total
    loading.value = false
  }
}
onMounted(() => {
  getList()
})

</script>

<style  scoped></style>
