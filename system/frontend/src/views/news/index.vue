<script lang="ts" setup>
import News from '@/components/NewsModal'
import Responsive from '@/components/ResponsiveModal'
import usePage from '@/hooks/usePage'

const filelist = ref([])
const token = localStorage.getItem('token')
const scrollRef = ref<HTMLElement | null>(null)
const { query } = useRoute()
const upRef = ref(null)
const base = import.meta.env.VITE_APP_URL
const text = ref('')
const timer = ref<ReturnType<typeof setTimeout> | null>(null)
const info = ref({ row: [] })
const { loading, page, total } = usePage()
const { proxy }: { proxy: CusProxy } = getCurrentInstance()

const customRequest = async ({ file, data, onFinish, headers, withCredentials, action, onError }: NaiveUI.UploadCustomRequestOptions) => {
  const formData = new FormData()
  if (data) {
    Object.keys(data).forEach(key => {
      formData.append(key, data[key])
    })
  }
  formData.append('file', file.file as File)
  const res = await fetch(action, {
    method: 'POST',
    headers,
    body: formData,
    withCredentials
  })
  const json = await res.json()
  if (json.code == 200) {
    file.url = json.data.fileUrl
    file.status = 'finished'
    onFinish()
  } else {
    onError()
  }
}

const viewNews = (id, num) => {
  const modal = window.$modal.create({
    title: proxy.$t('forum.replyCountTitle', { num }),
    preset: 'card',
    style: {
      width: 'min(680px, 92vw)'
    },
    content: () => h(News, { modal, id })
  })
}

const viewResponsive = (id, userId, invitationId, targetUserId, invitationReplyId, nickName, index, ia) => {
  const modal = window.$modal.create({
    title: proxy.$t('forum.replyTo', { name: nickName }),
    preset: 'card',
    style: {
      width: 'min(680px, 92vw)'
    },
    content: () => h(Responsive, { modal, id, userId, index, ia, sendSubComment, invitationId, targetUserId, invitationReplyId, nickName })
  })
}

const sendSubComment = async (index, ia, obj) => {
  info.value.row[index].numberComments += 1
  if (info.value.row[index].numberComments > 2) return
  if (info.value.row[index].replyRespList) {
    info.value.row[index].replyRespList.push(obj)
  } else {
    info.value.row[index].replyRespList = [obj]
  }
}

const onScrooll = () => {
  const scrollContainer = scrollRef.value
  if (!scrollContainer) return
  scrollContainer.addEventListener('scroll', () => {
    if (info.value.row.length >= total.value) return
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

const getlist = async () => {
  loading.value = true
  const res = await proxy.$api.forum.getCommentList({ ...page, invitationId: query.id })
  if (res.code === 200) {
    info.value.row = [...info.value.row, ...res.data.row]
    total.value = res.data.total
  }
  loading.value = false
}

const getInfo = async () => {
  const res = await proxy.$api.forum.getCommentInfo(query.id)
  if (res.code === 200) {
    info.value = { ...res.data, row: [] }
    getlist()
  }
}

const sendComment = async () => {
  const replyPictureList = filelist.value ? filelist.value.map(item => item.url) : []
  const res = await proxy.$api.forum.sendComment({ invitationId: info.value.id, replyContent: text.value, replyPictureList })
  if (res.code === 200) {
    info.value.row.unshift(res.data)
    info.value.comments += 1
    text.value = ''
    filelist.value = []
    upRef.value?.clear?.()
  }
}

const sendLike = async () => {
  const res = await proxy.$api.forum.addLike({ id: info.value.id, isLiked: !info.value.isLiked })
  if (res.code === 200) {
    info.value.isLiked = !info.value.isLiked
    info.value.likes += info.value.isLiked ? 1 : -1
  }
}

const roleLabel = (roleName: string) => roleName === '前台用户' ? proxy.$t('forum.student') : proxy.$t('forum.admin')
const roleClass = (roleName: string) => roleName === '前台用户' ? 'student' : 'admin'
const postTime = computed(() => info.value.createTime || info.value.createDate || info.value.updateTime || '')

onMounted(() => {
  getInfo()
  onScrooll()
})
</script>

<template>
  <main class="news-page ceres-gradient-bg" ref="scrollRef">
    <div class="news-shell ceres-page-shell">
      <article class="detail-card ceres-card">
        <header class="post-header">
          <n-avatar round :size="52" :src="info.avatar" />
          <div class="post-author">
            <div class="author-line">
              <strong>{{ info.nickName }}</strong>
              <span class="role-badge" :class="roleClass(info.roleName)">{{ roleLabel(info.roleName) }}</span>
            </div>
            <time v-if="postTime">{{ postTime }}</time>
          </div>
        </header>

        <p class="post-content">{{ info.content }}</p>

        <div v-if="info.pictureList && info.pictureList.length" class="image-gallery">
          <n-image-group show-toolbar-tooltip>
            <n-image v-for="(p, i) in info.pictureList" :key="i" width="190" :src="p" />
          </n-image-group>
        </div>

        <footer class="post-actions">
          <button type="button" class="feed-action" :class="{ active: info.isLiked }" @click="sendLike">
            <span class="iconify" :class="info.isLiked ? 'solar--like-bold' : 'solar--like-linear'"></span>
            <span>{{ info.likes }}</span>
          </button>
          <span class="feed-action readonly">
            <span class="iconify ic--outline-sms"></span>
            {{ $t('forum.comment') }} {{ info.comments }}
          </span>
        </footer>

        <section class="comment-panel">
          <div class="comment-composer">
            <n-upload
              ref="upRef"
              v-model:file-list="filelist"
              :action="`${base}/file/upload`"
              :headers="{ Authorization: token }"
              :data="{ folderName: 'image' }"
              :multiple="true"
              accept="image/*"
              :show-file-list="false"
              :custom-request="customRequest"
              class="upload-trigger"
            >
              <button type="button" class="comment-icon-button" :title="$t('forum.addImage')" :aria-label="$t('forum.addImage')">
                <span class="iconify solar--album-bold"></span>
              </button>
            </n-upload>
            <n-input v-model:value="text" size="medium" class="comment-input" :placeholder="$t('forum.commentPlaceholder')" />
            <button type="button" class="send-comment-button" @click="sendComment">{{ $t('forum.send') }}</button>
          </div>

          <div v-if="filelist.length" class="comment-images">
            <n-image-group show-toolbar-tooltip>
              <n-image v-for="item in filelist" :key="item.id" width="96" :src="item.url" />
            </n-image-group>
          </div>

          <div class="comment-list">
            <article v-for="(p, index) in info.row" :key="p.id" class="comment-card">
              <div class="comment-main">
                <n-avatar round :size="36" :src="p.avatar" class="shrink-0" />
                <div class="comment-body">
                  <div class="comment-meta">
                    <strong>{{ p.nickName }}</strong>
                    <button type="button" class="comment-reply" @click="viewResponsive(p.id, p.userId, p.invitationId, p.targetUserId, p.invitationReplyId, p.nickName, index, 0)">
                      <span class="iconify solar--chat-line-bold"></span>
                      {{ $t('forum.reply') }}
                    </button>
                  </div>
                  <p>{{ p.replyContent }}</p>
                  <div v-if="p.replyPictureList && p.replyPictureList.length" class="reply-images">
                    <n-image-group>
                      <n-image width="96" v-for="(pb, ib) in p.replyPictureList" :key="ib" :src="pb" />
                    </n-image-group>
                  </div>
                </div>
              </div>

              <div class="sub-replies" v-if="p.replyRespList && p.replyRespList.length">
                <article v-for="pb in p.replyRespList" :key="pb.id" class="sub-reply">
                  <div class="sub-reply-text">
                    <strong>{{ pb.nickName }}</strong>
                    <span>{{ $t('forum.reply') }}</span>
                    <strong>{{ pb.targetUserName }}</strong>
                    <span>{{ pb.replyContent }}</span>
                  </div>
                  <div v-if="pb.replyPictureList && pb.replyPictureList.length" class="reply-images">
                    <n-image-group>
                      <n-image v-for="(pd, id) in pb.replyPictureList" width="90" :key="id" :src="pd" />
                    </n-image-group>
                  </div>
                  <button type="button" class="comment-reply nested" @click="viewResponsive(pb.id, pb.userId, pb.invitationId, pb.targetUserId, pb.invitationReplyId, pb.nickName, index, 0)">
                    <span class="iconify solar--chat-line-bold"></span>
                    {{ $t('forum.reply') }}
                  </button>
                </article>
              </div>

              <button v-if="p.numberComments > 2" type="button" class="more-replies" @click="viewNews(p.id, p.numberComments)">{{ $t('forum.allReply', { num: p.numberComments }) }}</button>
            </article>
          </div>
        </section>
      </article>

      <div class="loading-row" v-show="loading">
        <span class="iconify eos-icons--three-dots-loading"></span>
      </div>
    </div>
  </main>
</template>

<style scoped>
.news-page {
  width: 100%;
  height: 100%;
  overflow: auto;
  background:
    radial-gradient(circle at 10% 8%, rgba(114, 46, 209, 0.12), transparent 30%),
    radial-gradient(circle at 90% 12%, rgba(6, 182, 212, 0.1), transparent 28%),
    linear-gradient(135deg, #f8f5ff 0%, #f8fbff 52%, #f3fff8 100%);
}

.news-shell {
  max-width: 920px;
  margin: 0 auto;
  padding: 1.2rem;
}

.detail-card {
  padding: 1.1rem;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 42px rgba(30, 41, 59, 0.08);
}

.post-header,
.author-line,
.post-actions,
.comment-composer,
.comment-meta,
.sub-reply-text {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.post-header {
  align-items: flex-start;
}

.post-author {
  min-width: 0;
  flex: 1;
}

.author-line strong,
.comment-meta strong,
.sub-reply-text strong {
  color: #111827;
}

.post-author time {
  display: block;
  margin-top: 0.18rem;
  color: #8a8fa3;
  font-size: 0.78rem;
}

.role-badge {
  min-height: 23px;
  padding: 0.14rem 0.5rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 800;
}

.role-badge.student {
  background: rgba(6, 182, 212, 0.1);
  color: #0e7490;
}

.role-badge.admin {
  background: rgba(114, 46, 209, 0.1);
  color: #5b21b6;
}

.post-content {
  margin: 1rem 0;
  color: #273142;
  font-size: 1rem;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-gallery,
.comment-images,
.reply-images {
  margin-top: 0.75rem;
}

.image-gallery :deep(.n-image) {
  overflow: hidden;
  margin: 0 0.55rem 0.55rem 0;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.post-actions {
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.9rem;
  padding-top: 0.85rem;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.feed-action,
.send-comment-button,
.comment-reply,
.more-replies {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  min-height: 34px;
  padding: 0.38rem 0.72rem;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  background: #fff;
  color: #536070;
  cursor: pointer;
  font-weight: 700;
  transition: transform 180ms ease, color 180ms ease, border-color 180ms ease, background-color 180ms ease, box-shadow 180ms ease;
}

.feed-action.readonly {
  cursor: default;
}

.feed-action:not(.readonly):hover,
.comment-reply:hover,
.more-replies:hover {
  transform: translateY(-1px);
  border-color: rgba(114, 46, 209, 0.2);
  color: #722ed1;
  background: rgba(114, 46, 209, 0.06);
}

.feed-action.active {
  border-color: rgba(114, 46, 209, 0.24);
  background: rgba(114, 46, 209, 0.08);
  color: #722ed1;
}

.comment-panel {
  margin-top: 1rem;
  padding: 0.95rem;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.96));
}

.comment-composer {
  align-items: stretch;
}

.comment-icon-button {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.05);
  color: #64748b;
  cursor: pointer;
  transition: background-color 180ms ease, color 180ms ease, transform 180ms ease;
}

.comment-icon-button:hover {
  transform: translateY(-1px);
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.comment-input {
  flex: 1;
}

.send-comment-button {
  min-width: 76px;
  border-color: transparent;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  box-shadow: 0 10px 20px rgba(114, 46, 209, 0.18);
}

.comment-list {
  display: grid;
  gap: 0.85rem;
  margin-top: 0.95rem;
}

.comment-card {
  padding: 0.8rem;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.comment-main {
  display: flex;
  gap: 0.7rem;
  align-items: flex-start;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-meta {
  justify-content: space-between;
  align-items: flex-start;
}

.comment-body p {
  margin: 0.35rem 0 0;
  color: #334155;
  line-height: 1.65;
  white-space: pre-wrap;
}

.comment-reply {
  min-height: 28px;
  padding: 0.24rem 0.55rem;
  font-size: 0.76rem;
}

.sub-replies {
  display: grid;
  gap: 0.55rem;
  margin: 0.75rem 0 0 3rem;
  padding-left: 0.75rem;
  border-left: 2px solid rgba(114, 46, 209, 0.12);
}

.sub-reply {
  padding: 0.65rem;
  border-radius: 12px;
  background: rgba(114, 46, 209, 0.045);
}

.sub-reply-text {
  align-items: baseline;
  flex-wrap: wrap;
  color: #536070;
  line-height: 1.55;
}

.comment-reply.nested,
.more-replies {
  margin-top: 0.45rem;
}

.more-replies {
  color: #5b21b6;
}

.loading-row {
  display: flex;
  justify-content: center;
  padding: 1rem;
  color: #722ed1;
  font-size: 1.4rem;
}

button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

@media (max-width: 720px) {
  .news-shell {
    padding: 0.75rem;
  }

  .post-header,
  .comment-composer {
    align-items: flex-start;
    flex-direction: column;
  }

  .send-comment-button,
  .comment-input {
    width: 100%;
  }

  .post-actions {
    justify-content: stretch;
  }

  .feed-action {
    flex: 1;
  }

  .sub-replies {
    margin-left: 0.5rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .news-page,
  .news-page * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
