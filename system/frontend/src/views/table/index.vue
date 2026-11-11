<template>
  <div class="p-4">
    <div class="border-0 border-l-4 border-solid border-green-400 text-2xl font-bold pl-4">表格示例</div>
    <div class="py-10">
      <XwyaTabel :columns="initColumns()" :data="data" :row-key="setRowKey" :onSelect="onSelect " :pagination="initPagination()" :loading="loading" />
    </div>

  </div>
</template>

<script setup lang="ts">
import usePage from '@/hooks/usePage'
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
const initColumns = () => {
  return [
    {
      type: 'selection',
      fixed: 'left'
    },
    {
      title: 'Name',
      key: 'name'
    },
    {
      title: 'Age',
      key: 'age'
    },
    {
      title: 'Sex',
      key: 'sex'
    },
    {
      title: 'Action',
      key: 'actions',
      render(row) {
        return h('div',
          {
            onClick: () => show(row)
          },
          { default: () => 'show' }
        )
      }
    }]
}
const initPagination = () => {
  return {
    itemCount: total.value,
    pageSizes: [10, 20],
    pageSlot: 7,
    pageSize:page.pageSize,
    page: page.pageNum,
    "onUpdate:page": (p: number) => {
      page.pageNum = p
      getList()
    },
  }
}
const getdata = () => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: arr,
        total: 300,
        msg: "请求成功"
      })
    }, 1000)
  })
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
const show = (row: any) => {
  window.$msg?.success(JSON.stringify(row))
}
const onSelect = (keys, rows) => {
  console.log(keys, rows);

}
const setRowKey = (obj: any) => {
  return obj.id
}

onMounted(() => {
  getList()
})

</script>

<style lang="scss" scoped></style>
