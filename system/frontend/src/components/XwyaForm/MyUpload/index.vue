<template>
   <n-form-item v-show="isShow" :label="label" :path="path" :rule="rules" :key="path+isShow">
    <n-upload
    v-model:file-list="value![path]"
     list-type="image-card"
     :custom-request="customRequest"
     v-bind="$attrs"
  />
  </n-form-item>
</template>

<script setup lang="ts">
const props =defineProps({
  value: {
    type: Object,
    default: void 0,
  },
  label: {
    type: String,
    default: ''
  },
  path: {
    type: String,
    default: ''
  },
  url: {
    type: String,
    default: ''
  },
  rules: {
    type: Array,
    default: () => []
  },
  isShow: {
    type: Boolean,
    default: true
  }

})
const customRequest = async ({file,data,onFinish,headers, withCredentials,onError,onProgress}: NaiveUI.UploadCustomRequestOptions) => {
  const formData = new FormData()
  if (data) {
    Object.keys(data).forEach(key => {
      formData.append(key, data[key])
    })
  }
  formData.append('file', file.file as File)
  headers.timeout = 60 * 60 * 1000
      const res  = await fetch(props.url, {
        method: 'POST',
        headers,
        body: formData,
        withCredentials
      })
  const json = await res.json()
  if (json.code == 200) {
    const url = '/staticc' + json.data.fileUrl.split('ene-test')[1]
    file.id = json.data.id
    file.url = url
    if (file.type.includes('video')) {
      const video = document.createElement('video')
      video.src = url
      video.onloadedmetadata = () => {
        props.value.duration = video.duration.toString()
      }
    }
    onFinish()
  } else {
    onError()
  }

    }
</script>

<style lang="scss" scoped></style>
