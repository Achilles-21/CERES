import { setModalTime } from "@/api/system"
export const useNews = (model) => {
  const onPageHide = () => {
    if (document.hidden) {
      setModalTime({ module: model, isClose: 0 })
    } else {
      setModalTime({ module: model, isClose: 1 })
    }
  }
  onMounted(() => {
    setModalTime({ module: model, isClose: 1 })
    document.addEventListener('visibilitychange', onPageHide);
  })
  onBeforeUnmount(() => {
    setModalTime({ module: model, isClose: 0 })
    document.removeEventListener('visibilitychange', onPageHide);
  })
}
