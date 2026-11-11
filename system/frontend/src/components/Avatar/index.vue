<script lang="ts" setup>
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { updateAvatar} from "@/api/system"
import i18n from '@/lang'
const props = defineProps(["close","initInfo","avatar"])
const editorRef = ref(null)
const filelist = ref([])
const imageUrl = ref(props.avatar)
const imgStyle = ref({})
const src = computed(() => {
  if (filelist.value.length) {
    return filelist.value[0].url
  }
  return ''
})

const fileToBase64 = (file) => {

    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);   
      reader.onload = () => resolve(reader.result);   
      reader.onerror = error => reject(error);        
    });
  }
const customRequest = async ({file,onFinish,headers, withCredentials,onError,onProgress}: NaiveUI.UploadCustomRequestOptions) => {

    const url = await fileToBase64(file.file)
    file.url = url
    onFinish()
}
const onRealTime = (data) => {
  const zoom = 110/data.w
  data.img.zoom = zoom
  imgStyle.value = data.img
  imageUrl.value=data.url
}
const onImageReset = () => {
  filelist.value = []
  imageUrl.value = ''
  imgStyle.value = {}
}
function base64ToBlob(base64Data) {
  const arr = base64Data.split(',');
  const mime = arr[0].match(/:(.*?);/)[1];  
  const bstr = atob(arr[1]);  
  let n = bstr.length;
  const u8arr = new Uint8Array(n);

  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }

  return new Blob([u8arr], { type: mime });
}

const onSubmit = () => {
   editorRef.value.getCropData((data) => {
     const blob = base64ToBlob(data)
     const file = new File([blob], 'avatar.png', { type: 'image/png' })
     upImage(file)

   })

}
const upImage = async (file) => {
    const formData = new FormData()
  formData.append('folderName', 'avatar')
  formData.append('file', file as File)
      const res  = await fetch(import.meta.env.VITE_APP_URL+'/file/upload', {
        method: 'POST',
        headers: {
          'Authorization': localStorage.getItem('token'),
        },
        body: formData,
      })
  const json = await res.json()
  if (json.code == 200) {
    const ress = await updateAvatar({ imageUrl: json.data.fileUrl })
    if (ress.code === 200) {
      props.initInfo()
      props.close()
    }
  }
}

</script>

<template>
  <div>
    <div class="flex gap-8">
    <div class="w-[300px] aspect-square">
      <n-upload  v-show="!filelist.length"  :trigger-style="{height:'300px',width:'300px'}" :custom-request="customRequest" :max="1" v-model:file-list="filelist"   list-type="image-card"   accept="image/*" />
      <VueCropper  v-if="filelist.length" :autoCrop="true"  :fixed="true"   :auto-crop-width="100" :auto-crop-height="100" :limitMinSize="100" :img="src"   :outputSize="1"  outputType="png"
      ref="editorRef"  @realTime="onRealTime"   />

    </div>
    <div class=" w-[110px] h-[110px] rounded-full overflow-hidden bg-gray-300">
      <img :src="imageUrl" class="h-full w-full"  :style="imgStyle" alt="">
    </div>
  </div>
  <div class="mt-2 text-blue-500  cursor-pointer" @click="onImageReset"> {{ i18n.global.t('userPage.reupload') }} </div>
  </div>
  <div class="flex justify-end gap-4">
    <n-button @click="close">{{ i18n.global.t('system.cancel') }}</n-button>
    <n-button type="success" @click="onSubmit">{{ i18n.global.t('system.confirm') }}</n-button>
  </div>
</template>

<style scoped>
</style>
