<script lang="ts" setup>
import Responsive from '@/components/ResponsiveModal'
import usePage from '@/hooks/usePage'
import { getAllCommentList } from '@/api/forum'
import i18n from '@/lang'

const props = defineProps(['modal', 'id'])
const scrollRef = ref<HTMLElement | null>(null)
const timer = ref<ReturnType<typeof setTimeout> | null>(null)
const { loading, data, total, page } = usePage()

const replies = computed(() => data.value.replyCommentRespList || [])

const viewResponsive = (id, userId, invitationId, targetUserId, invitationReplyId, nickName, index, ia) => {
  const modal = window.$modal.create({
    title: `${i18n.global.t('forum.reply')}${nickName}`,
    preset: 'card',
    style: {
      width: 'min(680px, 92vw)'
    },
    content: () => h(Responsive, { modal, id, userId, invitationId, targetUserId, invitationReplyId, nickName, index, ia, sendSubComment })
  })
}

const getlist = async () => {
  loading.value = true
  const res = await getAllCommentList({ ...page, invitationReplyId: props.id })
  if (res.code === 200 && res.data.row[0]) {
    const row = res.data.row[0]
    const oldReplies = data.value.replyCommentRespList || []
    data.value = { ...row, ...data.value, replyCommentRespList: [...oldReplies, ...(row.replyCommentRespList || [])] }
    total.value = row.numberComments
  }
  loading.value = false
}

const onScrooll = () => {
  const scrollContainer = scrollRef.value
  if (!scrollContainer) return
  scrollContainer.addEventListener('scroll', () => {
    if (replies.value.length >= total.value) return
    if (scrollContainer.scrollTop + scrollContainer.clientHeight >= scrollContainer.scrollHeight) {
      if (timer.value) return
      timer.value = setTimeout(async () => {
        page.pageNum++
        await getlist()
        timer.value = null
      }, 100)
    }
  })
}

const sendSubComment = (ia, ib, obj) => {
  data.value.replyCommentRespList.push(obj)
}

onMounted(() => {
  onScrooll()
  getlist()
})
</script>

<template>
  <section class="news-modal" ref="scrollRef">
    <article class="root-comment">
      <n-avatar round :size="40" :src="data.avatar" class="shrink-0" />
      <div class="root-body">
        <div class="root-meta">
          <strong>{{ data.nickName }}</strong>
          <button type="button" class="reply-button" @click="viewResponsive(data.id, data.userId, data.invitationId, data.targetUserId, data.invitationReplyId, data.nickName, 0, 0)">
            <span class="iconify solar--chat-line-bold"></span>
            {{ i18n.global.t('forum.reply') }}
          </button>
        </div>
        <p>{{ data.replyContent }}</p>
      </div>
    </article>

    <div class="reply-list">
      <article v-for="(p, ia) in replies" :key="p.id" class="reply-card">
        <div class="reply-copy">
          <strong>{{ p.nickName }}</strong>
          <span>{{ i18n.global.t('forum.reply') }}</span>
          <strong>{{ p.targetUserName }}</strong>
          <span>{{ p.replyContent }}</span>
        </div>
        <div v-if="p.replyPictureList && p.replyPictureList.length" class="reply-images">
          <n-image-group>
            <n-image v-for="(pb, ib) in p.replyPictureList" width="96" :key="ib" :src="pb" />
          </n-image-group>
        </div>
        <button type="button" class="reply-button nested" @click="viewResponsive(p.id, p.userId, p.invitationId, p.targetUserId, p.invitationReplyId, p.nickName, ia, 0)">
          <span class="iconify solar--chat-line-bold"></span>
          {{ i18n.global.t('forum.reply') }}
        </button>
      </article>
    </div>

    <div class="loading-row" v-show="loading">
      <span class="iconify eos-icons--three-dots-loading"></span>
    </div>
  </section>
</template>

<style scoped>
.news-modal {
  max-height: 520px;
  overflow: auto;
  padding-right: 0.25rem;
}

.root-comment {
  position: sticky;
  top: 0;
  z-index: 1;
  display: flex;
  gap: 0.75rem;
  padding: 0.85rem;
  border: 1px solid rgba(114, 46, 209, 0.13);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 26px rgba(30, 41, 59, 0.08);
}

.root-body {
  min-width: 0;
  flex: 1;
}

.root-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.root-meta strong,
.reply-copy strong {
  color: #111827;
}

.root-body p {
  margin: 0.35rem 0 0;
  color: #334155;
  line-height: 1.65;
  white-space: pre-wrap;
}

.reply-list {
  display: grid;
  gap: 0.65rem;
  margin-top: 0.75rem;
  padding-left: 1rem;
  border-left: 2px solid rgba(114, 46, 209, 0.12);
}

.reply-card {
  padding: 0.75rem;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  background: linear-gradient(180deg, #fff, #fbfbff);
}

.reply-copy {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.45rem;
  color: #536070;
  line-height: 1.6;
}

.reply-images {
  margin-top: 0.6rem;
}

.reply-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  min-height: 30px;
  padding: 0.28rem 0.62rem;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  background: #fff;
  color: #536070;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 800;
  transition: transform 180ms ease, color 180ms ease, border-color 180ms ease, background-color 180ms ease;
}

.reply-button:hover {
  transform: translateY(-1px);
  border-color: rgba(114, 46, 209, 0.22);
  background: rgba(114, 46, 209, 0.06);
  color: #722ed1;
}

.reply-button.nested {
  margin-top: 0.5rem;
}

.loading-row {
  display: flex;
  justify-content: center;
  padding: 0.8rem;
  color: #722ed1;
  font-size: 1.3rem;
}

button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

@media (prefers-reduced-motion: reduce) {
  .news-modal,
  .news-modal * {
    transition: none !important;
  }
}
</style>
