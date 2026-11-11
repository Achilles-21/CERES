<template>
  <div class="p-4">
    <XwyaPullRefresh :loading="loading" :load="load"  :list ="list" :finished="finished" :refresh="onRefresh" />
    <span>CERES</span>
  </div>
</template>

<script setup lang="ts">
const page = reactive({
  pagenum: 1,
  pagesize: 10
})
const total = ref(1)
const list = ref([])
const loading = ref(true)
const finished = ref(true)
const data = () => {
  return new Promise<void>((resolve, reject) => {

    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
          { name: '张三', age: 18, address: '北京' },
        ],
        total: 40
      })
    }, 2000);
  })
}
const getData = async () => {
  const res = await data()
  if(page.pagenum == 1) {
    list.value = []
  }
  list.value = [...list.value, ...res.data]
  total.value = res.total
  loading.value=false
}
const load = async () => {
  loading.value=true
    await getData()
    page.pagenum++
  if (list.value.length >= total.value) {
    finished.value=false
    loading.value = false
  }
}
const onRefresh = async () => {
  console.log("触发下拉刷新");
  page.pagenum = 1
  total.value = 1
  finished.value=true
  await getData()
}
</script>

<style scoped></style>
