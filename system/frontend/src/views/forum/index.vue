<script lang="ts" setup>
import News from '@/components/NewsModal'
import Responsive from '@/components/ResponsiveModal'
import usePage from '@/hooks/usePage'
import useSystemStore from '@/store/useSystemStore'

const base = import.meta.env.VITE_APP_URL
const token = localStorage.getItem('token')
const scrollRef = ref<HTMLElement | null>(null)
const timer = ref<ReturnType<typeof setTimeout> | null>(null)
const { proxy }: { proxy: CusProxy } = getCurrentInstance()
const { loading, data, page, total } = usePage()
const { userInfo } = useSystemStore()
const { push } = useRouter()

const getlist = async () => {
  loading.value = true
  const res = await proxy.$api.forum.getForumList({ ...page })
  if (res.code === 200) {
    data.value = [...data.value, ...res.data.row]
    total.value = res.data.total
  }
  loading.value = false
}

const customRequest = async ({ file, data: myData, onFinish, headers, withCredentials, action, onError }: NaiveUI.UploadCustomRequestOptions) => {
  const formData = new FormData()
  if (myData) {
    Object.keys(myData).forEach(key => {
      formData.append(key, myData[key])
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
    title: `${proxy.$t('forum.reply')}${nickName}`,
    preset: 'card',
    style: {
      width: 'min(680px, 92vw)'
    },
    content: () => h(Responsive, { modal, id, userId, index, ia, sendSubComment, invitationId, targetUserId, invitationReplyId, nickName })
  })
}

const onScrooll = () => {
  const scrollContainer = scrollRef.value
  if (!scrollContainer) return
  scrollContainer.addEventListener('scroll', () => {
    if (data.value.length >= total.value) return
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

const deletes = async (id, index) => {
  const res = await proxy.$api.forum.delForum(id)
  if (res.code === 200) {
    data.value.splice(index, 1)
  }
}

const viewComment = async (index, id) => {
  if (data.value[index].view) {
    data.value[index].view = false
    return
  }
  const res = await proxy.$api.forum.getCommentList({ pageSize: 2, pageNum: 1, invitationId: id })
  if (res.code === 200) {
    data.value[index].view = true
    data.value[index].filelist = data.value[index].filelist || []
    res.data.row.forEach(item => {
      item.filelist = []
    })
    data.value[index].row = res.data.row
    data.value[index].total = res.data.total
  }
}

const sendComment = async (index) => {
  const replyPictureList = data.value[index].filelist ? data.value[index].filelist.map(item => item.url) : []
  const res = await proxy.$api.forum.sendComment({ invitationId: data.value[index].id, replyContent: data.value[index].text, replyPictureList })
  if (res.code === 200) {
    if (data.value[index].row) {
      if (data.value[index].row.length === 2) {
        data.value[index].row.pop()
      }
      data.value[index].row.unshift(res.data)
    } else {
      data.value[index].row = [res.data]
    }
    data.value[index].comments += 1
    data.value[index].text = ''
    data.value[index].filelist = []
    const uploadRef = proxy.$refs.upCommentRef
    const uploader = Array.isArray(uploadRef) ? uploadRef[index] : uploadRef
    uploader?.clear?.()
  }
}

const sendSubComment = async (index, ia, obj) => {
  data.value[index].row[ia].numberComments += 1
  if (data.value[index].row[ia].numberComments > 2) return
  if (data.value[index].row[ia].replyRespList) {
    data.value[index].row[ia].replyRespList.push(obj)
  } else {
    data.value[index].row[ia].replyRespList = [obj]
  }
}

const sendLike = async (index) => {
  const res = await proxy.$api.forum.addLike({ id: data.value[index].id, isLiked: !data.value[index].isLiked })
  if (res.code === 200) {
    data.value[index].isLiked = !data.value[index].isLiked
    data.value[index].likes += data.value[index].isLiked ? 1 : -1
  }
}

const toDetail = (index) => {
  push(`/news?id=${data.value[index].id}`)
}

const canDeleteComment = (comment) => userInfo?.userId === 1 || userInfo?.userId === comment?.userId
const removedCommentCount = (comment) => 1 + Math.max(Number(comment?.numberComments || 0), comment?.replyRespList?.length || 0)
const decreasePostCommentCount = (item, count = 1) => {
  item.comments = Math.max((item.comments || 0) - count, 0)
}

const deleteComment = (index, ia) => {
  const item = data.value[index]
  const comment = item?.row?.[ia]
  if (!item || !comment) return
  window.$dialog.warning({
    title: proxy.$t('forum.deleteComment'),
    content: proxy.$t('forum.deleteCommentConfirm'),
    positiveText: proxy.$t('system.clearConfirm'),
    negativeText: proxy.$t('system.clearCancel'),
    onPositiveClick: async () => {
      const res = await proxy.$api.forum.delComment(comment.id)
      if (res.code === 200) {
        item.row.splice(ia, 1)
        decreasePostCommentCount(item, removedCommentCount(comment))
      }
    }
  })
}

const deleteSubComment = (index, ia, subIndex) => {
  const item = data.value[index]
  const comment = item?.row?.[ia]
  const subComment = comment?.replyRespList?.[subIndex]
  if (!item || !comment || !subComment) return
  window.$dialog.warning({
    title: proxy.$t('forum.deleteComment'),
    content: proxy.$t('forum.deleteCommentConfirm'),
    positiveText: proxy.$t('system.clearConfirm'),
    negativeText: proxy.$t('system.clearCancel'),
    onPositiveClick: async () => {
      const res = await proxy.$api.forum.delComment(subComment.id)
      if (res.code === 200) {
        comment.replyRespList.splice(subIndex, 1)
        comment.numberComments = Math.max((comment.numberComments || 0) - 1, 0)
        decreasePostCommentCount(item)
      }
    }
  })
}

const roleLabel = (roleName: string) => roleName === '前台用户' ? proxy.$t('forum.student') : proxy.$t('forum.admin')
const roleClass = (roleName: string) => roleName === '前台用户' ? 'student' : 'admin'
const postTime = (item) => item.createTime || item.createDate || item.updateTime || ''

onMounted(() => {
  getlist()
  onScrooll()
})
</script>

<template>
  <main class="forum-page ceres-gradient-bg" ref="scrollRef">
    <div class="forum-shell ceres-page-shell">
      <header class="feed-header ceres-glass">
        <div>
          <p class="eyebrow">{{ $t('forum.feedEyebrow') }}</p>
          <h1>{{ $t('forum.feedTitle') }}</h1>
          <span>{{ $t('forum.feedSubtitle') }}</span>
        </div>
        <button type="button" class="publish-button ceres-pill" @click="$router.push('/postWritings')">
          <span class="iconify solar--pen-new-square-bold"></span>
          {{ $t('forum.goPublish') }}
        </button>
      </header>

      <section class="feed-list" :aria-label="$t('forum.feedListAria')">
        <article v-for="(item, index) in data" :key="item.id" class="feed-card ceres-card">
          <header class="post-header">
            <n-avatar round :size="48" :src="item.avatar" />
            <div class="post-author">
              <div class="author-line">
                <strong>{{ item.nickName }}</strong>
                <span class="role-badge" :class="roleClass(item.roleName)">{{ roleLabel(item.roleName) }}</span>
              </div>
              <time v-if="postTime(item)">{{ postTime(item) }}</time>
            </div>
            <button type="button" class="icon-action danger" :title="$t('pageGlobal.delete')" :aria-label="$t('forum.deletePost')" @click="deletes(item.id, index)">
              <span class="iconify solar--trash-bin-trash-bold"></span>
            </button>
          </header>

          <p class="post-content">{{ item.content }}</p>

          <div v-if="item.pictureList && item.pictureList.length" class="image-gallery">
            <n-image-group show-toolbar-tooltip>
              <n-image v-for="(p, i) in item.pictureList" :key="i" width="180" :src="p" />
            </n-image-group>
          </div>

          <footer class="post-actions" :aria-label="$t('forum.postActionsAria')">
            <button type="button" class="feed-action" :class="{ active: item.isLiked }" @click="sendLike(index)">
              <span class="iconify" :class="item.isLiked ? 'solar--like-bold' : 'solar--like-linear'"></span>
              <span>{{ item.likes }}</span>
            </button>
            <button type="button" class="feed-action" @click="viewComment(index, item.id)">
              <span class="iconify ic--outline-sms"></span>
              <span>{{ $t('forum.comment') }} {{ item.comments }}</span>
              <span class="iconify arrow" :class="item.view ? 'ic--outline-keyboard-arrow-up' : 'ic--outline-keyboard-arrow-down'"></span>
            </button>
          </footer>

          <Transition name="comment-panel">
            <section v-if="item.view" class="comment-panel">
              <div class="comment-composer">
                <n-upload
                  ref="upCommentRef"
                  v-model:file-list="item.filelist"
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
                <n-input v-model:value="item.text" size="medium" class="comment-input" :placeholder="$t('forum.contentTips')" />
                <button type="button" class="send-comment-button" @click="sendComment(index)">{{ $t('forum.send') }}</button>
              </div>

              <div v-if="item.filelist && item.filelist.length" class="comment-images">
                <n-image-group show-toolbar-tooltip>
                  <n-image v-for="ps in item.filelist" :key="ps.id" width="96" :src="ps.url" />
                </n-image-group>
              </div>

              <div class="comment-list">
                <article v-for="(p, ia) in item.row" :key="p.id || ia" class="comment-card">
                  <div class="comment-main">
                    <n-avatar round :size="36" :src="p.avatar" class="shrink-0" />
                    <div class="comment-body">
                      <div class="comment-meta">
                        <strong>{{ p.nickName }}</strong>
                        <div class="comment-actions">
                          <button type="button" class="comment-reply" @click="viewResponsive(p.id, p.userId, p.invitationId, p.targetUserId, p.invitationReplyId, p.nickName, index, ia)">
                            <span class="iconify solar--chat-line-bold"></span>
                            {{ $t('forum.reply') }}
                          </button>
                          <button
                            v-if="canDeleteComment(p)"
                            type="button"
                            class="comment-delete"
                            :title="$t('forum.deleteComment')"
                            :aria-label="$t('forum.deleteComment')"
                            @click="deleteComment(index, ia)"
                          >
                            <span class="iconify solar--trash-bin-trash-bold" aria-hidden="true"></span>
                          </button>
                        </div>
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
                    <article v-for="(pc, subIndex) in p.replyRespList" :key="pc.id" class="sub-reply">
                      <div class="sub-reply-text">
                        <strong>{{ pc.nickName }}</strong>
                        <span>{{ $t('forum.reply') }}</span>
                        <strong>{{ pc.targetUserName }}</strong>
                        <span>{{ pc.replyContent }}</span>
                      </div>
                      <div v-if="pc.replyPictureList && pc.replyPictureList.length" class="reply-images">
                        <n-image-group>
                          <n-image width="90" v-for="(pd, id) in pc.replyPictureList" :key="id" :src="pd" />
                        </n-image-group>
                      </div>
                      <div class="sub-reply-actions">
                        <button type="button" class="comment-reply nested" @click="viewResponsive(pc.id, pc.userId, pc.invitationId, pc.targetUserId, pc.invitationReplyId, pc.nickName, index, ia)">
                          <span class="iconify solar--chat-line-bold"></span>
                          {{ $t('forum.reply') }}
                        </button>
                        <button
                          v-if="canDeleteComment(pc)"
                          type="button"
                          class="comment-delete"
                          :title="$t('forum.deleteComment')"
                          :aria-label="$t('forum.deleteComment')"
                          @click="deleteSubComment(index, ia, subIndex)"
                        >
                          <span class="iconify solar--trash-bin-trash-bold" aria-hidden="true"></span>
                        </button>
                      </div>
                    </article>
                  </div>

                  <button v-if="p.numberComments > 2" type="button" class="more-replies" @click="viewNews(p.id, p.numberComments)">
                    {{ $t('forum.allReply', { num: p.numberComments }) }}
                  </button>
                </article>
              </div>

              <button v-if="item.comments > 2" type="button" class="view-all" @click="toDetail(index)">{{ $t('forum.viewReply') }}</button>
            </section>
          </Transition>
        </article>
      </section>

      <div class="loading-row" v-show="loading">
        <span class="iconify eos-icons--three-dots-loading"></span>
      </div>
    </div>
  </main>
</template>

<style scoped>
.forum-page {
  width: 100%;
  height: 100%;
  overflow: auto;
  background:
    radial-gradient(circle at 8% 8%, rgba(114, 46, 209, 0.12), transparent 30%),
    radial-gradient(circle at 92% 12%, rgba(6, 182, 212, 0.1), transparent 28%),
    linear-gradient(135deg, #f8f5ff 0%, #f8fbff 52%, #f3fff8 100%);
}

.forum-shell {
  max-width: 980px;
  margin: 0 auto;
  padding: 1.2rem;
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 1rem 1.1rem;
  border: 1px solid rgba(114, 46, 209, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 50px rgba(55, 48, 163, 0.12);
  backdrop-filter: blur(18px);
}

.eyebrow {
  margin: 0;
  color: #722ed1;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.feed-header h1 {
  margin: 0.2rem 0 0.2rem;
  color: #111827;
  font-size: clamp(1.4rem, 2vw, 2rem);
  letter-spacing: 0;
}

.feed-header > div > span {
  color: #667085;
  font-size: 0.9rem;
}

.publish-button {
  min-height: 40px;
  padding: 0.55rem 0.95rem;
  border: 0;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  cursor: pointer;
  font-weight: 800;
  box-shadow: 0 14px 28px rgba(114, 46, 209, 0.22);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.publish-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 34px rgba(114, 46, 209, 0.28);
}

.feed-list {
  display: grid;
  gap: 1rem;
}

.feed-card {
  padding: 1rem;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(30, 41, 59, 0.08);
  animation: feedIn 240ms ease-out;
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

.icon-action,
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

.icon-action:hover,
.comment-icon-button:hover {
  transform: translateY(-1px);
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.icon-action.danger:hover {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.post-content {
  margin: 0.9rem 0;
  color: #273142;
  font-size: 0.98rem;
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
.more-replies,
.view-all {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  background: #fff;
  color: #536070;
  cursor: pointer;
  font-weight: 700;
  transition: transform 180ms ease, color 180ms ease, border-color 180ms ease, background-color 180ms ease, box-shadow 180ms ease;
}

.feed-action {
  min-height: 34px;
  padding: 0.38rem 0.72rem;
}

.feed-action:hover,
.comment-reply:hover,
.more-replies:hover,
.view-all:hover {
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

.arrow {
  font-size: 1rem;
}

.comment-panel {
  display: grid;
  gap: 0.95rem;
  margin-top: 0.95rem;
  padding: 0.95rem;
  min-width: 0;
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.96));
}

.comment-composer {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: stretch;
  gap: 0.65rem;
}

.upload-trigger {
  width: 34px;
  min-width: 34px;
  flex-shrink: 0;
}

.comment-input {
  width: 100%;
  min-width: 0;
  flex: 1;
}

.send-comment-button {
  min-width: 76px;
  padding: 0.38rem 0.82rem;
  flex-shrink: 0;
  border-color: transparent;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  white-space: nowrap;
  box-shadow: 0 10px 20px rgba(114, 46, 209, 0.18);
}

.comment-list {
  display: grid;
  gap: 0.85rem;
  min-width: 0;
}

.comment-card {
  padding: 0.8rem;
  min-width: 0;
  overflow: hidden;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.comment-main {
  display: flex;
  gap: 0.7rem;
  min-width: 0;
  align-items: flex-start;
}

.comment-body {
  min-width: 0;
  flex: 1;
  overflow-wrap: anywhere;
}

.comment-meta {
  min-width: 0;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  row-gap: 0.35rem;
}

.comment-body p {
  margin: 0.35rem 0 0;
  color: #334155;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
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
  min-width: 0;
  padding-left: 0.75rem;
  border-left: 2px solid rgba(114, 46, 209, 0.12);
}

.sub-reply {
  padding: 0.65rem;
  min-width: 0;
  overflow: hidden;
  border-radius: 12px;
  background: rgba(114, 46, 209, 0.045);
}

.comment-actions,
.sub-reply-actions {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  flex-shrink: 0;
}

.comment-delete {
  display: inline-flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(220, 38, 38, 0.16);
  border-radius: 999px;
  background: rgba(220, 38, 38, 0.06);
  color: #dc2626;
  cursor: pointer;
  transition: transform 180ms ease, color 180ms ease, border-color 180ms ease, background-color 180ms ease;
}

.comment-delete:hover {
  transform: translateY(-1px);
  border-color: rgba(220, 38, 38, 0.28);
  background: rgba(220, 38, 38, 0.1);
}

.sub-reply-text {
  min-width: 0;
  align-items: baseline;
  flex-wrap: wrap;
  color: #536070;
  line-height: 1.55;
  overflow-wrap: anywhere;
}

.sub-reply-text span:last-child {
  flex-basis: 100%;
  min-width: 0;
  color: #334155;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.sub-reply-actions {
  margin-top: 0.45rem;
}

.more-replies,
.view-all {
  min-height: 32px;
  margin-top: 0.65rem;
  padding: 0.34rem 0.72rem;
  color: #5b21b6;
}

.view-all {
  width: 100%;
}

.loading-row {
  display: flex;
  justify-content: center;
  padding: 1rem;
  color: #722ed1;
  font-size: 1.4rem;
}

.comment-panel-enter-active,
.comment-panel-leave-active {
  transition: opacity 220ms ease, transform 220ms ease, max-height 220ms ease;
  overflow: hidden;
}

.comment-panel-enter-from,
.comment-panel-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-6px);
}

.comment-panel-enter-to,
.comment-panel-leave-from {
  max-height: 900px;
  opacity: 1;
  transform: translateY(0);
}

@keyframes feedIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

@media (max-width: 720px) {
  .forum-shell {
    padding: 0.75rem;
  }

  .feed-header,
  .post-header,
  .comment-composer {
    align-items: flex-start;
    flex-direction: column;
  }

  .publish-button,
  .send-comment-button,
  .comment-input {
    width: 100%;
  }

  .comment-composer {
    grid-template-columns: 34px minmax(0, 1fr);
    align-items: stretch;
  }

  .send-comment-button {
    grid-column: 1 / -1;
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
  .forum-page,
  .forum-page * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
