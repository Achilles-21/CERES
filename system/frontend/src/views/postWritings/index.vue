<script lang="ts" setup>
const value = ref('')
const filelist = ref([])
const base = import.meta.env.VITE_APP_URL
const token = localStorage.getItem('token')
const upRef = ref(null)
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

const sendPosts = async () => {
  if (value.value == '') {
    window.$msg.error(proxy.$t('forum.textTips'))
    return
  }
  const pictureList = filelist.value.map((item: any) => item.url)
  const res = await proxy.$api.forum.sendForum({ content: value.value, pictureList })
  if (res.code === 200) {
    filelist.value = []
    value.value = ''
    upRef.value?.clear?.()
    window.$msg.success(proxy.$t('forum.okPublish'))
  }
}
</script>

<template>
  <main class="post-page ceres-gradient-bg">
    <div class="post-shell ceres-page-shell">
      <header class="post-header ceres-glass">
        <div>
          <p class="eyebrow">{{ $t('forum.createPost') }}</p>
          <h1>{{ $t('forum.publish') }}</h1>
          <span>{{ $t('forum.publishSubtitle') }}</span>
        </div>
        <button type="button" class="ghost-button ceres-pill" @click="$router.back()">
          <span class="iconify solar--arrow-left-linear"></span>
          {{ $t('forum.back') }}
        </button>
      </header>

      <section class="composer-card ceres-card">
        <label class="field-block">
          <span>{{ $t('forum.text') }}</span>
          <n-input
            v-model:value="value"
            type="textarea"
            class="post-textarea"
            :autosize="{ minRows: 5, maxRows: 8 }"
            :placeholder="$t('forum.textTips')"
          />
        </label>

        <div class="field-block">
          <div class="field-heading">
            <span>{{ $t('forum.upImg') }}</span>
            <small>{{ $t('forum.imageCount', { num: filelist.length }) }}</small>
          </div>
          <div class="upload-card">
            <n-upload
              ref="upRef"
              v-model:file-list="filelist"
              :action="`${base}/file/upload`"
              :headers="{ Authorization: token }"
              :data="{ folderName: 'image' }"
              :multiple="true"
              accept="image/*"
              :custom-request="customRequest"
              list-type="image-card"
            />
          </div>
        </div>

        <footer class="composer-actions">
          <button type="button" class="ghost-button" @click="$router.back()">{{ $t('forum.cancel') }}</button>
          <button type="button" class="publish-button" @click="sendPosts">
            <span class="iconify solar--plain-bold"></span>
            {{ $t('forum.publish') }}
          </button>
        </footer>
      </section>
    </div>
  </main>
</template>

<style scoped>
.post-page {
  width: 100%;
  height: 100%;
  overflow: auto;
  background:
    radial-gradient(circle at 8% 8%, rgba(114, 46, 209, 0.12), transparent 30%),
    radial-gradient(circle at 90% 12%, rgba(16, 185, 129, 0.1), transparent 28%),
    linear-gradient(135deg, #f8f5ff 0%, #f8fbff 52%, #f3fff8 100%);
}

.post-shell {
  max-width: 920px;
  margin: 0 auto;
  padding: 1.2rem;
}

.post-header {
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

.post-header h1 {
  margin: 0.2rem 0 0.2rem;
  color: #111827;
  font-size: clamp(1.4rem, 2vw, 2rem);
  letter-spacing: 0;
}

.post-header > div > span {
  color: #667085;
  font-size: 0.9rem;
}

.composer-card {
  display: grid;
  gap: 1rem;
  padding: 1.1rem;
  border: 1px solid rgba(99, 102, 241, 0.13);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 42px rgba(30, 41, 59, 0.08);
}

.field-block {
  display: grid;
  gap: 0.55rem;
  color: #111827;
  font-weight: 800;
}

.field-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.field-heading small {
  color: #697386;
  font-size: 0.78rem;
  font-weight: 700;
}

.post-textarea :deep(.n-input-wrapper),
.upload-card {
  border-radius: 14px;
}

.upload-card {
  padding: 1rem;
  border: 1px dashed rgba(114, 46, 209, 0.22);
  background: linear-gradient(135deg, rgba(114, 46, 209, 0.045), rgba(6, 182, 212, 0.045));
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.65rem;
  padding-top: 0.5rem;
}

.ghost-button,
.publish-button {
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.48rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 800;
  transition: transform 180ms ease, box-shadow 180ms ease, background-color 180ms ease, color 180ms ease;
}

.ghost-button {
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  color: #536070;
}

.publish-button {
  border: 0;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  box-shadow: 0 14px 28px rgba(114, 46, 209, 0.22);
}

.ghost-button:hover,
.publish-button:hover {
  transform: translateY(-1px);
}

.ghost-button:hover {
  color: #722ed1;
  background: rgba(114, 46, 209, 0.06);
}

.publish-button:hover {
  box-shadow: 0 18px 34px rgba(114, 46, 209, 0.28);
}

button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.18);
}

@media (max-width: 720px) {
  .post-shell {
    padding: 0.75rem;
  }

  .post-header,
  .composer-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .ghost-button,
  .publish-button {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .post-page,
  .post-page * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
