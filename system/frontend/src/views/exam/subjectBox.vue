<script lang="ts" setup>
import usePage from '@/hooks/usePage';
import FavoriteQuestionCard from './components/FavoriteQuestionCard.vue';
const props = defineProps(["changeInfo"])
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any
const { total, page, data } = usePage(20)
const list = ref([])
const active = ref(false)
type QuestionId = string | number
const expandedQueIds = ref<QuestionId[]>([])
const dataInfo = reactive({
  loading: true,
  finished: true
})

const updateFavoritePageState = () => {
  if (data.value.length >= total.value) {
    dataInfo.finished = false
    return
  }
  dataInfo.finished = true
  page.pageNum++
}
const load = async () => {
  dataInfo.loading = true
  await getLikeList(page.pageNum > 1)
  updateFavoritePageState()
  dataInfo.loading = false
}
const onRefresh = async () => {
  page.pageNum = 1
  total.value = 0
  dataInfo.finished = true
  expandedQueIds.value = []
  await getLikeList(false)
  updateFavoritePageState()
}
const getLikeList = async (append = false) => {
  const res = await proxy.$api.question.getLikeQuestionList(page)
  const rows = res.data ? res.data.row || [] : []
  data.value = append ? [...data.value, ...rows] : rows
  total.value = res.data ? res.data.total : 0
  dataInfo.loading = false
}
const isFavoriteExpanded = (queId: QuestionId) => expandedQueIds.value.includes(queId)
const closeFavoriteCard = (queId: QuestionId) => {
  expandedQueIds.value = expandedQueIds.value.filter(id => id !== queId)
}
const toggleFavoriteCard = (queId: QuestionId) => {
  if (isFavoriteExpanded(queId)) {
    closeFavoriteCard(queId)
    return
  }
  expandedQueIds.value = [...expandedQueIds.value, queId]
}
const changeShow = async () => {
  console.log("显示发请求");
}
const changeStatus = async (a: string, i: number) => {

  const res = await proxy.$api.question.sendAnswer({queRecordReqs:[{ queId: i, answer: a }]})
  if (res.code == 200) {
    const index = list.value.findIndex(item => item.queId === i)
    list.value[index].disabled = true
  }
}
const onLike = async (i: number) => {

  if (list.value[i].favoriteFlag == "0") {
    const res = await proxy.$api.question.addLikeQuestion({queId:list.value[i].queId})
    if (res.code === 200) {
      list.value[i].favoriteFlag = "1"
    }
    return
  }
  const res = await proxy.$api.question.deleteLikeQuestion({queId:list.value[i].queId})
  if (res.code === 200) {
    list.value[i].favoriteFlag = "0"
  }
}
const onCancelLike = async (queId: QuestionId, i: number) => {
  const res = await proxy.$api.question.deleteLikeQuestion({ queId })
  if (res.code === 200) {
    closeFavoriteCard(queId)
    data.value.splice(i, 1)
    total.value = Math.max(total.value - 1, 0)
    const index = list.value.findIndex(item => item.id == queId || item.queId == queId)
    if (index > -1) {
      list.value[index].favoriteFlag = "0"
    }
  }
}
const sendQue = (ques) => {

  list.value = ques
  getNoteInfo(ques[0].courseId)
}
const getNoteInfo = async (id) => {
  const res = await proxy.$api.note.getNote({ videoId: id});
  if (res.code == 200) {
    const obj = {
      videoId: id,
      id: res.data.id,
      v: res.data.content,
    }
    props.changeInfo(obj)

  } else {
    window.$msg.destroyAll()
  }
}

onMounted(() => {
  proxy.$mitt.on('sendQue',sendQue)
})
onBeforeUnmount(() => {
  proxy.$mitt.off('sendQue',sendQue)
})

</script>

<template>
  <div class="font-cjk-page flex flex-col w-full h-full">
    <div class="h-14 border-0 border-b border-solid border-gray-200 bg-white flex items-center justify-between px-4 gap-4">
      <button class="exam-back-button" type="button" aria-label="Back" @click="$router.back()">
        <span class="iconify ic--baseline-keyboard-arrow-left text-4xl" aria-hidden="true"></span>
      </button>
      <button class="exam-favorites-button" type="button" :aria-label="$t('system.favorites')" @click="active = true">
        <span class="exam-favorites-icon">
          <span class="iconify ic--baseline-folder-special text-xl" aria-hidden="true"></span>
        </span>
        <span class="exam-favorites-label">{{ $t('system.favorites') }}</span>
      </button>
    </div>
    <n-scrollbar class="exam-question-scroll flex-1 w-full bg-white">
      <XwyaSubjectType :data="p" :index="i" :like="onLike" :change="changeStatus" v-for="(p, i) in list" :key="p.queId" />
      <div  v-if="!list.length">
        <n-empty :description="$t('system.examTips')" />
      </div>
    </n-scrollbar>
    <n-drawer block-scroll v-model:show="active" width="min(92vw, 420px)" placement="left" class="exam-favorites-drawer" @after-enter="changeShow">
      <n-drawer-content class="exam-favorites-panel">
        <template #header>
          <div class="favorites-drawer-header">
            <span class="favorites-drawer-icon">
              <span class="iconify ic--baseline-folder-special" aria-hidden="true"></span>
            </span>
            <span class="favorites-drawer-copy">
              <strong>{{ proxy.$t('system.favorites') }}</strong>
              <small>{{ data.length }} / {{ total }}</small>
            </span>
          </div>
        </template>

        <XwyaPullRefresh class="favorites-refresh" :loading="dataInfo.loading" :load="load" :finished="dataInfo.finished" :refresh="onRefresh">
          <div class="favorites-list">
            <FavoriteQuestionCard
              v-for="(p, i) in data"
              :key="p.queId || i"
              :question="p"
              :expanded="isFavoriteExpanded(p.queId)"
              @toggle="toggleFavoriteCard"
              @remove="queId => onCancelLike(queId, i)"
            />
          </div>

          <div v-if="!data.length && !dataInfo.loading" class="favorites-empty">
            <span class="iconify solar--clipboard-remove-bold" aria-hidden="true"></span>
            <n-empty :description="$t('system.examTips')" />
          </div>
        </XwyaPullRefresh>

      </n-drawer-content>
    </n-drawer>
  </div>

</template>

<style lang="css" scoped>
.exam-question-scroll {
  padding: 18px;
}

.exam-back-button {
  display: inline-flex;
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font: inherit;
  transition: background-color 200ms ease, color 200ms ease;
}

.exam-back-button:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.exam-back-button:focus-visible {
  outline: 2px solid rgba(8, 145, 178, 0.35);
  outline-offset: 2px;
}

.exam-favorites-button {
  display: inline-flex;
  min-width: 0;
  min-height: 38px;
  align-items: center;
  gap: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
  color: #164e63;
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  padding: 0 14px 0 10px;
  transition: border-color 200ms ease, box-shadow 200ms ease, color 200ms ease, background-color 200ms ease;
}

.exam-favorites-button:hover {
  border-color: #67e8f9;
  box-shadow: 0 10px 22px rgba(8, 145, 178, 0.14);
  color: #0e7490;
}

.exam-favorites-button:focus-visible {
  outline: 2px solid rgba(8, 145, 178, 0.35);
  outline-offset: 2px;
}

.exam-favorites-icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #ecfeff;
  color: #0891b2;
}

.exam-favorites-label {
  line-height: 1;
  white-space: nowrap;
}

:deep(.exam-favorites-drawer.n-drawer) {
  border-radius: 0 18px 18px 0;
  box-shadow: 22px 0 60px rgba(15, 23, 42, 0.16);
}

:deep(.exam-favorites-drawer .n-drawer-content.n-drawer-content--native-scrollbar) {
  scrollbar-color: rgba(8, 145, 178, 0.38) rgba(236, 254, 255, 0.72);
  scrollbar-width: thin;
}

:deep(.exam-favorites-drawer .n-drawer-content.n-drawer-content--native-scrollbar::-webkit-scrollbar) {
  width: 8px;
}

:deep(.exam-favorites-drawer .n-drawer-content.n-drawer-content--native-scrollbar::-webkit-scrollbar-track) {
  background: rgba(236, 254, 255, 0.72);
}

:deep(.exam-favorites-drawer .n-drawer-content.n-drawer-content--native-scrollbar::-webkit-scrollbar-thumb) {
  border: 2px solid rgba(236, 254, 255, 0.72);
  border-radius: 999px;
  background: rgba(8, 145, 178, 0.38);
}

:deep(.exam-favorites-drawer .n-drawer-content.n-drawer-content--native-scrollbar::-webkit-scrollbar-thumb:hover) {
  background: rgba(8, 145, 178, 0.56);
}

:deep(.exam-favorites-panel.n-drawer-content) {
  overflow: hidden;
  border-right: 1px solid rgba(8, 145, 178, 0.16);
  background:
    radial-gradient(circle at 12% 0%, rgba(34, 197, 94, 0.14), transparent 18rem),
    linear-gradient(180deg, rgba(236, 254, 255, 0.96), rgba(248, 250, 252, 0.98) 42%, #ffffff);
  color: #0f172a;
}

:deep(.exam-favorites-panel .n-drawer-header) {
  border-bottom: 1px solid rgba(8, 145, 178, 0.13);
  padding: 16px 16px 14px;
}

:deep(.exam-favorites-panel .n-drawer-header__main) {
  min-width: 0;
  width: 100%;
}

:deep(.exam-favorites-panel .n-drawer-body-content-wrapper) {
  padding: 0;
}

.favorites-drawer-header {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.favorites-drawer-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(103, 232, 249, 0.62);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(236, 254, 255, 0.94)),
    #ecfeff;
  color: #0891b2;
  font-size: 22px;
  box-shadow: 0 12px 26px rgba(8, 145, 178, 0.14);
}

.favorites-drawer-copy {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.favorites-drawer-copy strong {
  overflow: hidden;
  color: #164e63;
  font-size: 17px;
  font-weight: 900;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorites-drawer-copy small {
  color: #64748b;
  font-family: 'AppLatin', system-ui, -apple-system, sans-serif;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.3;
}

.favorites-refresh {
  display: block;
  min-height: 100%;
}

.favorites-list {
  display: grid;
  gap: 10px;
  padding: 14px;
}

.favorites-empty {
  display: grid;
  min-height: 220px;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #64748b;
  padding: 24px;
}

.favorites-empty > .iconify {
  color: #0891b2;
  font-size: 38px;
}

@media (max-width: 768px) {
  .exam-question-scroll {
    padding: 14px;
  }
}
@media (max-width: 480px) {
  .exam-favorites-button {
    padding-right: 12px;
  }

  .favorites-list {
    padding: 12px;
  }
}
:deep .text-lg { font-size: 16px; }
:deep .text-xl { font-size: 16px; }
:deep .n-radio__label { font-size: 14px; }

:deep .analysis-block,
:deep .analysis-block * {
  font-size: 13px !important;
  font-family: 'AppLatin', system-ui, -apple-system, sans-serif !important;
}
</style>
