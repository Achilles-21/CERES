<script lang="ts" setup>
import { sendComment } from '@/api/forum'
import i18n from '@/lang'

const props = defineProps(['modal', 'id', 'userId', 'index', 'ia', 'sendSubComment', 'invitationId', 'targetUserId', 'invitationReplyId', 'userInfo', 'nickName'])
const base = import.meta.env.VITE_APP_URL
const value = ref('')
const filelist = ref([])
const token = localStorage.getItem('token')

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

const onOk = async () => {
  if (!value.value.trim() && filelist.value.length === 0) {
    window.$msg.error(i18n.global.t('forum.replyTips'))
    return
  }
  const replyPictureList = filelist.value.map(item => item.url)
  const res = await sendComment({
    invitationId: props.invitationId,
    replyContent: value.value,
    replyPictureList,
    invitationReplyId: props.invitationReplyId ? props.invitationReplyId : props.id,
    targetUserId: props.userId,
    targetCommentId: props.id
  })
  if (res.code === 200) {
    props.sendSubComment(props.index, props.ia, res.data)
    props.modal.destroy()
  }
}
</script>

<template>
  <section class="responsive-modal">
    <div class="reply-target">
      <span class="iconify solar--reply-2-bold"></span>
      <div>
        <small>{{ i18n.global.t('forum.reply') }}</small>
        <strong>{{ nickName }}</strong>
      </div>
    </div>

    <n-input
      v-model:value="value"
      type="textarea"
      class="reply-textarea"
      :autosize="{ minRows: 4, maxRows: 7 }"
      :placeholder="i18n.global.t('forum.replyTips')"
    />

    <div v-if="filelist.length" class="preview-images">
      <n-image-group show-toolbar-tooltip>
        <n-image v-for="item in filelist" :key="item.id" width="96" :src="item.url" />
      </n-image-group>
    </div>

    <footer class="modal-actions">
      <n-upload
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
        <button type="button" class="tool-button" :title="i18n.global.t('forum.addImage')" :aria-label="i18n.global.t('forum.addImage')">
          <span class="iconify solar--album-bold"></span>
        </button>
      </n-upload>

      <button type="button" class="reply-submit" @click="onOk">
        <span class="iconify solar--plain-bold"></span>
        {{ i18n.global.t('forum.reply') }}
      </button>
    </footer>
  </section>
</template>

<style scoped>
.responsive-modal {
  display: grid;
  gap: 0.9rem;
}

.reply-target {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.8rem;
  border: 1px solid rgba(114, 46, 209, 0.13);
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.06), rgba(6, 182, 212, 0.06));
}

.reply-target .iconify {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(114, 46, 209, 0.11);
  color: #722ed1;
  font-size: 1.2rem;
}

.reply-target small {
  display: block;
  color: #697386;
  font-size: 0.74rem;
  line-height: 1.4;
}

.reply-target strong {
  color: #111827;
}

.reply-textarea :deep(.n-input-wrapper) {
  border-radius: 14px;
}

.preview-images {
  padding: 0.8rem;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.9);
}

.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.tool-button,
.reply-submit {
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 800;
  transition: transform 180ms ease, box-shadow 180ms ease, background-color 180ms ease, color 180ms ease;
}

.tool-button {
  width: 36px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  color: #536070;
}

.reply-submit {
  padding: 0.45rem 0.9rem;
  border: 0;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  box-shadow: 0 12px 24px rgba(114, 46, 209, 0.22);
}

.tool-button:hover,
.reply-submit:hover {
  transform: translateY(-1px);
}

.tool-button:hover {
  color: #722ed1;
  background: rgba(114, 46, 209, 0.06);
}

.reply-submit:hover {
  box-shadow: 0 16px 30px rgba(114, 46, 209, 0.28);
}

button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

@media (max-width: 520px) {
  .modal-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .tool-button,
  .reply-submit,
  .upload-trigger {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .responsive-modal,
  .responsive-modal * {
    transition: none !important;
  }
}
</style>
