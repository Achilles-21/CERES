<template>
  <div class="">
    <div class="flex m-10 ">
      <XwyaForm class="w-5/6" label-placement="left" :item-list="userQueryFormItem" v-model="queryFormData" :row="5">
      <template #default="{ change, state }">
        <n-button :type="!state ? 'primary' : 'error'" @click="search(state, change)">
          {{ state ? $t(`pageGlobal.cancel`) : $t(`pageGlobal.search`)  }}
        </n-button>
      </template>
    </XwyaForm>
    <div class="w-1/6  flex justify-end">
      <n-tooltip trigger="hover">
        <template #trigger>
          <XwyaButton @click="showModel($t(`pageGlobal.add`),false)" type="info" icon="iconify gg--add  text-xl text-white"  />
        </template>
        {{ $t(`pageGlobal.add`) }}
      </n-tooltip>
    </div>
    </div>

    <div class="pt-4">

      <XwyaTabel :columns="initColumns()" :data="data"   :pagination="initPagination()" :loading="loading" :row-key="(u) => { return u.userId }" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import usePage from '@/hooks/usePage'
import { useUserQueryForm } from '@/hooks/useQueryForm'
import Bntlist from '@/components/BntList'
import AddForm from './modelForm.vue'
const arr = [
  { id: 1, name: "xw", age: 18, sex: "男" },
  { id: 2, name: "xw", age: 18, sex: "男" },
  { id: 3, name: "xw", age: 18, sex: "男" },
  { id: 4, name: "xw", age: 18, sex: "男" },
  { id: 5, name: "xw", age: 18, sex: "男" },
  { id: 6, name: "xw", age: 18, sex: "男" },
  { id: 7, name: "xw", age: 18, sex: "男" },
  { id: 8, name: "xw", age: 18, sex: "男" },
  { id: 9, name: "xw", age: 18, sex: "男" },
  { id: 10, name: "xw", age: 18, sex: "男" },
  { id: 11, name: "xw", age: 18, sex: "男" },
]
const { page, total, loading, data } = usePage()
const { queryFormData, userQueryFormItem } = useUserQueryForm()
const {proxy} = getCurrentInstance()

const search = (state, change) => {
  if (state) {
    window.$msg.success('搜索')
  } else {
    queryFormData.value = {}
    window.$msg.success('取消搜索')
  }
  change()
}
const initColumns = () => {
  return [
    {
      type: 'selection',
      fixed: 'left'
    },
    {
      title: proxy.$t('pageGlobal.number'),
      key: 'id'
    },
    {
      title: proxy.$t('userPage.name'),
      key: 'age'
    },
    {
      title: proxy.$t('login.account'),
      key: 'age'
    },
    {
      title: proxy.$t('userPage.createDate'),
      key: 'sex'
    },
    {
      title: proxy.$t('pageGlobal.operation'),
      key: 'actions',
      render(row) {
        return h(Bntlist,{onEdit:()=>showModel(proxy.$t(`pageGlobal.edit`),true),onDelete:onDelete})
      }
    }]
}

const onDelete =  () => {
  console.log("删除");
}

const showModel = (title: string,flag:boolean) => {
 const modal= window.$modal.create({
    title,
   preset: 'card',
   maskClosable:false,
    style: {
      width:"600px"
   },
   content: () => h(AddForm, {flag,modal,id,getList})
  })
}
const initPagination = () => {
  return {
    itemCount: total.value,
    pageSizes: [10, 20],
    pageSlot: 7,
    page: page.pageNum,
    "onUpdate:page": (p: number) => {
      page.pageNum = p
      getList()
    },
  }
}

const getList = async () => {
  loading.value = true
  const res = await getdata()
  if (res.code === 200) {
    data.value = res.data
    total.value = res.total
    window.$msg?.success(res.msg)
    loading.value = false
  }

}
onMounted(() => {
  getList()
})

</script>

<style  scoped></style>
