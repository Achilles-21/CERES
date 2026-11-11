const usePage = (pageSize: number=10) => {
  const page = reactive({
    pageSize,
    pageNum: 1,
  })
  const data = ref([])
  const total:number = ref(0)
  const loading = ref(false)
  const reset = () => { page.pageNum = 1 }
  return {
    page,
    total,
    loading,
    data,
    reset
  }
}
export default usePage
