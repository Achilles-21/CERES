<script lang="ts" setup>
import videojs from "video.js"
import zhCN from 'video.js/dist/lang/zh-CN.json'
import 'video.js/dist/video-js.css'
import ModalQue from "@/components/ModalQue";
import i18n from "@/lang";
const { proxy }: { proxy: CusProxy } = getCurrentInstance()
const { query } = useRoute()
const video = shallowRef(null)
const videoElement = shallowRef(null)
const imageUrl = ref("")
const askList = ref([])
const popId = ref(0)  
const props = defineProps({
  changeNote: {
    type: Function,
    default: () => { }
  },
  data: {
    type: Object,
    default: () => { }
  },
  disable: {
    type: Boolean,
    default: false
  }
})
const sliderInfo = reactive({
  sliderVal: 0,
  sliderMax: 120,
  totalDuration: "00:00",
  currentDuration: "00:00"
})
const videoState = reactive({
  isPlaying: false,
  isMuted: false,
  currentVolume: 1,
  modalShow: false,

})
const isVideoReady = ref(false)
const videoError = ref(false)
const hasVideo = computed(() => Boolean(props.data?.videoUrl))
const hasVideoPayload = computed(() => Boolean(props.data && typeof props.data === 'object' && Object.keys(props.data).length))
const isInitialLoading = computed(() => !hasVideo.value && !hasVideoPayload.value)
const showEmptyState = computed(() => !hasVideo.value && hasVideoPayload.value)
const showVideoLoading = computed(() => hasVideo.value && !isVideoReady.value && !videoError.value)
const isDrag = ref(true)
const config = computed(() => (
  {
    autoplay: true,
    muted: isIE() || props.disable,
    width: '100%',
    language: "zh-CN",
    preload: 'auto',
    sources: [
      {
        type: "video/mp4",
        src: props.data.videoUrl
      }
    ],
  }
))

function isIE() {
  const userAgent = window.navigator.userAgent;

  const isIE = userAgent.indexOf('MSIE ') > -1 || userAgent.indexOf('Trident/') > -1;

  const isEdge = userAgent.indexOf('Edge/') > -1 || userAgent.indexOf('Edg/') > -1;
  return isIE || isEdge;

}
const initVideo = () => {
  isVideoReady.value = false
  videoError.value = false
  videojs.addLanguage('zh-CN', zhCN);
  video.value = videojs("xwya-video", config.value);
  video.value.on('loadedmetadata', () => {
    isVideoReady.value = true

    const duration = video.value.duration();
    if (duration == props.data.playingTime) {
      video.value.pause();
    }
    videoState.isMuted = video.value.muted();
    video.value.currentTime(props.data.playingTime)
    sliderInfo.sliderMax = duration
    formatTime(duration, "totalDuration")
  });
  video.value.on('canplay', () => {
    isVideoReady.value = true
  });
  video.value.on('error', () => {
    isVideoReady.value = false
    videoError.value = true
  });
  video.value.on('timeupdate', () => {
    if (isDrag.value) {
      const currentTime = video.value.currentTime();
      const teacherImages = props.data.teacherImageRespList || []
      for (let i = 0; i < teacherImages.length; i++) {
        if (currentTime >= teacherImages[i].imageTime) {
          imageUrl.value = teacherImages[i].teacherImage
          break;
        }
      }
      sliderInfo.sliderVal = currentTime
      formatTime(currentTime, "currentDuration")
      if (props.data.videoPopRespList && props.data.videoPopRespList.length > 0) {
        for (let i = 0; i < props.data.videoPopRespList.length; i++) {

          if (currentTime > parseInt(props.data.videoPopRespList[i].questionTime[0]) && currentTime < parseInt(props.data.videoPopRespList[i].questionTime[1]) && !props.data.videoPopRespList[i].isAnswer) {
            if (!props.data.videoPopRespList[i].modalShow) {
              props.data.videoPopRespList[i].modalShow = true
              video.value.pause()
              if(props.data.videoPopRespList[i].isShow) {
                popId.value = props.data.videoPopRespList[i].id
                const askData = props.data.videoPopRespList[i].queList
                const strList = ['A', 'B', 'C', 'D', 'E', 'F']
                for(let item of askData) {
                  const option = []
                  for(let str of strList) {
                    item['choice'+str] && option.push({
                      choice: item['choice'+str],
                      title: str
                    })
                  }
                  if(item.type == '1') {
                    item.answer = item.answer.split(' ')
                  }
                  item.options = option
                }
                askList.value = askData
              }

              nextTick(() => {
                changeStyle()
                for(let item of props.data.videoPopRespList[i].queList) {
                  if(item.type == '2') {
                    proxy.$mitt.emit("setPamas", {
                      content: item.content,
                      answer: item.answerExplain,
                      close: () => {
                      },
                      queId: item.id
                    })
                    break
                  }
                }
              })

            }
          }
        }
      }



    }
  });
  video.value.on('play', () => {
    videoState.isPlaying = true;
  });

  video.value.on('pause', () => {
    videoState.isPlaying = false;
  });
  video.value.on('volumechange', () => {
    videoState.isMuted = video.value.muted();
    videoState.currentVolume = video.value.volume();
  });
  videoElement.value = document.getElementById('xwya-video');
  videoElement.value.addEventListener('keydown', handleKeyPress);

}

const changeStyle = () => {

}

const changeStatus = (a:string,i:number) => {
  for(let item of askList.value) {
    if(item.id == i) {
      item.disabled = true
    }
  }
  checkAnswer()
}

const clearAskList = () => {
  for(let ask of askList.value) {
    if(!ask.disabled && ask.type != '2') {
      window.$msg.error(i18n.global.t('question.needAnswer'))
      return
    }
  }
  askList.value = []
  changeVideoState()
}

const changeVideoState = () => {
  if (video.value.paused()) {
    video.value.play();
  } else {
    video.value.pause();
  }
}
const changeVideoVoice = () => {
  if (video.value) {
    video.value.muted(!video.value.muted());
  }
}
const onDragstart = () => {
  if (video.value) isDrag.value = false
}
const onDragend = () => {
  if (video.value) {
    isDrag.value = true
    video.value.currentTime(sliderInfo.sliderVal);
  }
}
const onUpdate = (value: number) => {
  formatTime(value, "currentDuration")
}
const formatTime = (seconds: number, name: string): string => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = Math.floor(seconds % 60);
  if (hours > 0) {
    sliderInfo[name] = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
  } else {
    sliderInfo[name] = `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
  }
}
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.code === 'Space') {
    event.preventDefault();  
    changeVideoState();
  }
};
const sendTime = async () => {
  await proxy.$api.video.updetaVideoTime({ id: query.id, playingTime: video.value.currentTime() })
}
watch(() => props.data, (n) => {
  if (n?.videoUrl) {
    initVideo()
  } else {
    isVideoReady.value = false
    videoError.value = false
  }
})

const videoSrc = ref("")
proxy.$mitt.on("setVideo", (e:any) => {
  videoSrc.value = e.src
  for(let item of askList.value) {
    if(item.type == '2') {
      item.disabled = true
      item.isanswer = e.userAnswer
      item.isPop = e.isPop
    }
  }
  checkAnswer()
})

const checkAnswer = () => {
  const request = []
  let hasType2 = false
  for(let item of askList.value) {
    if(!item.disabled) return
    const requestItem = {
      queId:item.id,
      queContent:item.content,
      userOptions:item.isanswer,
      popId:popId.value,
      result: 0
    }
    if(item.type == '0') {
      requestItem.result = item.isanswer == item.answer ? 1 : 0
    } else if(item.type == '1') {
      const answer = item.answer.split(' ')
      const userAnswer = item.isanswer.split(' ')
      if(answer.length == userAnswer.length) {
        for(let a of userAnswer) {
          if(answer.includes(a)) {
            requestItem.result += 1
          }
        }
      }
      requestItem.result = requestItem.result == answer.length ? 1 : 0
    } else if(item.type == '2') {
      hasType2 = true
      requestItem.result = item.isPop
      video.value.pause()
    }
    request.push(requestItem)
  }
  if(hasType2) askList.value = []
  proxy.$api.question.setVideoRecord(request)
}

const continuePlay = () => {
  video.value.play()
  videoSrc.value = ''
}

onBeforeUnmount(() => {
  if (videoElement.value && video.value) {
    sendTime()
    video.value.dispose()
    videoElement.value.removeEventListener('keydown', handleKeyPress);
  }
})
</script>

<template>
  <input id="popId" type="hidden" :value="popId" />
  <div class="video-shell ceres-card" :class="{ 'is-disabled': props.disable }">
    <div v-if="props.disable" class="video-protection-layer"></div>
    <video v-if="props.disable" muted autoplay id="xwya-video" class="video-element"></video>
    <video v-else @click="changeVideoState" id="xwya-video" class="video-element"></video>

    <div v-if="isInitialLoading || showVideoLoading" class="video-state video-state-overlay" aria-busy="true">
      <span class="iconify eos-icons--three-dots-loading" aria-hidden="true"></span>
      <strong>Loading video</strong>
      <p>Preparing the medical learning player</p>
    </div>
    <div v-else-if="showEmptyState || videoError" class="video-state">
      <span class="iconify solar--video-library-bold" aria-hidden="true"></span>
      <strong>{{ $t('system.nodata') }}</strong>
      <p>Video source is not available</p>
    </div>

    <div v-show="videoState.isPlaying && imageUrl" class="teacher-overlay">
      <img :src="imageUrl" class="teacher-image" alt="Teacher reference" />
    </div>

    <div class="ask-layout" v-if="askList.length">
      <n-scrollbar class="ask-card ceres-card">
        <XwyaSubjectType :data="p" :index="i" :showLike="true" :hideInputer="true" :change="changeStatus" v-for="(p, i) in askList" :key="p.queId" />
        <div class="ask-actions">
          <n-button type="primary" @click="clearAskList">{{ i18n.global.t('video.continue') }}</n-button>
        </div>
      </n-scrollbar>
    </div>

    <div class="video-popover-layout" v-if="videoSrc">
      <div class="video-popover-card ceres-card">
        <button class="ceres-icon-button video-popover-close" type="button" aria-label="Close video" title="Close video" @click="continuePlay">
          <span class="iconify ic--baseline-close" aria-hidden="true"></span>
        </button>
        <video :src="videoSrc" controls autoplay></video>
      </div>
    </div>

    <div class="video-controls-container" v-if="!props.disable && hasVideo && !videoError">
      <div class="video-controls ceres-glass">
        <n-slider
          class="control-slider"
          @update:value="onUpdate"
          @dragstart="onDragstart"
          @dragend="onDragend"
          v-model:value="sliderInfo.sliderVal"
          :max="sliderInfo.sliderMax"
          :step="0.1"
          :format-tooltip="() => sliderInfo.currentDuration"
        />
        <div class="video-control-row">
          <div class="video-control-left">
            <button class="video-control-button" type="button" :aria-label="videoState.isPlaying ? 'Pause' : 'Play'" @click="changeVideoState">
              <SvgIcon :name="videoState.isPlaying ? 'play' : 'pause'" className="!text-xl"></SvgIcon>
            </button>
            <button class="video-control-button" type="button" :aria-label="videoState.isMuted ? 'Unmute' : 'Mute'" @click="changeVideoVoice">
              <SvgIcon :name="videoState.isMuted ? 'mute' : 'voice'" className="!text-xl"></SvgIcon>
            </button>
            <span class="video-time">{{ sliderInfo.currentDuration }} / {{ sliderInfo.totalDuration }}</span>
          </div>
          <button class="note-control ceres-pill" type="button" @click="changeNote">
            <span class="iconify solar--notes-bold" aria-hidden="true"></span>
            <span>{{ i18n.global.t('pastlist.notes') }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.video-shell {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 280px;
  overflow: hidden;
  border-color: rgba(114, 46, 209, 0.16);
  border-radius: 24px;
  background:
    radial-gradient(circle at 18% 14%, rgba(139, 71, 234, 0.22), transparent 28%),
    linear-gradient(135deg, #020617, #111827 54%, #1e1b4b);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.22);
}

.video-element,
#xwya-video {
  display: block;
  width: 100%;
  height: 100%;
  background: #020617;
  color: #fff;
  object-fit: contain;
}

:deep(.vjs-tech) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-protection-layer {
  position: absolute;
  inset: 0;
  z-index: 70;
  background: rgba(2, 6, 23, 0.82);
  backdrop-filter: blur(2px);
}

.video-state {
  position: absolute;
  inset: 0;
  z-index: 55;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(2, 6, 23, 0.96), rgba(49, 46, 129, 0.9));
  color: #e2e8f0;
  text-align: center;
}

.video-state-overlay {
  background: rgba(2, 6, 23, 0.58);
  backdrop-filter: blur(8px);
}

.video-state .iconify {
  color: #c4b5fd;
  font-size: 44px;
}

.video-state strong {
  color: #fff;
  font-size: 18px;
  font-weight: 850;
}

.video-state p {
  margin: 0;
  color: #cbd5e1;
  font-size: 14px;
}

.teacher-overlay {
  position: absolute;
  right: clamp(16px, 3vw, 40px);
  bottom: 88px;
  z-index: 45;
  width: clamp(92px, 11vw, 130px);
  aspect-ratio: 1;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  background: rgba(15, 23, 42, 0.72);
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.28);
}

.teacher-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ask-layout,
.video-popover-layout {
  position: absolute;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(16px, 4vw, 30px);
  background: rgba(2, 6, 23, 0.56);
  backdrop-filter: blur(8px);
}

.ask-card {
  width: min(760px, 100%);
  max-height: min(88%, 680px);
  padding: clamp(16px, 3vw, 30px);
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.96);
}

.ask-actions {
  display: flex;
  justify-content: center;
  padding-top: 16px;
}

.video-popover-card {
  position: relative;
  display: flex;
  width: min(980px, 94vw);
  max-height: 88%;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-color: rgba(255, 255, 255, 0.18);
  background: #020617;
}

.video-popover-card video {
  width: 100%;
  max-height: 86vh;
  background: #000;
  object-fit: contain;
}

.video-popover-close {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  cursor: pointer;
  background: rgba(15, 23, 42, 0.78);
  color: #fff;
}

.video-controls-container {
  position: absolute;
  right: 14px;
  bottom: 14px;
  left: 14px;
  z-index: 60;
}

.video-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-color: rgba(255, 255, 255, 0.22);
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.74);
  padding: 8px 12px 10px;
  color: #fff;
}

.control-slider {
  width: 100%;
}

.video-control-row,
.video-control-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.video-control-row {
  justify-content: space-between;
}

.video-control-left {
  min-width: 0;
}

.video-control-button {
  display: inline-flex;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  cursor: pointer;
  transition: border-color 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.video-control-button:hover,
.video-control-button:focus-visible {
  border-color: rgba(196, 181, 253, 0.76);
  background: rgba(139, 71, 234, 0.28);
  box-shadow: 0 10px 26px rgba(139, 71, 234, 0.24);
  outline: none;
}

.video-time {
  overflow: hidden;
  color: #e2e8f0;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-control {
  min-height: 36px;
  flex-shrink: 0;
  border-color: rgba(196, 181, 253, 0.36);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  padding: 0 14px;
}

.note-control:hover,
.note-control:focus-visible {
  border-color: rgba(196, 181, 253, 0.72);
  background: rgba(139, 71, 234, 0.28);
  color: #fff;
  outline: none;
}

@media (max-width: 900px) {
  .video-shell {
    height: auto;
    min-height: clamp(220px, 56vw, 420px);
    aspect-ratio: 16 / 9;
  }

  .teacher-overlay {
    bottom: 80px;
  }
}

@media (max-width: 560px) {
  .video-controls-container {
    right: 8px;
    bottom: 8px;
    left: 8px;
  }

  .video-controls {
    border-radius: 16px;
    padding: 7px 8px 8px;
  }

  .video-control-row {
    align-items: stretch;
    flex-direction: column;
    gap: 8px;
  }

  .video-control-left {
    justify-content: space-between;
  }

  .note-control {
    width: 100%;
  }

  .teacher-overlay {
    right: 12px;
    bottom: 116px;
    width: 82px;
    border-radius: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .video-control-button,
  .note-control {
    transition: none;
  }
}
</style>
