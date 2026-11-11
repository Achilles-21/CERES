<script lang="ts" setup>
import { ref, defineProps, onMounted, nextTick, onBeforeUnmount } from 'vue'
import i18n from '@/lang'
const props = defineProps({
  isVisualizing:{
    type: Boolean,
    default: false
  },
  analyser:{
    type: Object,
    default: () => { }
  },
  v:{
    type: Object,
    default: () => { }
  }
})

let animationFrameId:number|null = null
const cav = ref(null)
const draw = (ctx:any, bufferLength:any, dataArray:any) => {
  if (!props.isVisualizing) return;  
  props.analyser.getByteFrequencyData(dataArray);
  animationFrameId = requestAnimationFrame(() => { draw(ctx, bufferLength, dataArray) });
  ctx.clearRect(0, 0, cav.value.width, cav.value.height);
  const barWidth = 5
  let barHeight;
  let x = 0;
  for (let i = 0; i < bufferLength; i++) {
    barHeight = dataArray[i];
    ctx.fillStyle = 'rgb(' + 255 + ',255,255)';
    ctx.fillRect(x, cav.value.height - barHeight / 2, barWidth, barHeight / 2);
    x += barWidth + 1;
  }
};
const start = () => {

  try {
    const ctx = cav.value.getContext('2d');
    const bufferLength = props.analyser.frequencyBinCount
    const dataArray = new Uint8Array(bufferLength);
    draw(ctx, bufferLength, dataArray);
  } catch (error) {
    console.log(error);
    props.v.errorHandle(error)
    nextTick(() => {
      props.v.stop(false)
    })
  }
}
onMounted(() => {
  start()
})
onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
})
</script>

<template>
  <Teleport to='body'>
    <div class="audio_wave_container">
      <div class="audio_wave_packing">
        <canvas id="wave" ref="cav"></canvas>
        <div class="audio_wave_text_container">
          <p class="audio_wave_confirm" @click="() => v.stop(true)">{{ i18n.global.t('system.confirm') }}</p>
          <p class="audio_wave_cancel" @click="() => v.stop(false)">{{ i18n.global.t('system.cancel') }}</p>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style lang="css" scoped>
.audio_wave_container {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
}

.audio_wave_packing {
  border-radius: 20px;
  box-sizing: border-box;
  padding: 20px;
  position: absolute;
  width: 180px;
  height: 180px;
  display: flex;
  flex-flow: column;
  background-color: rgba(0, 0, 0, .6);
}

#wave {
  flex: 1;
}

.audio_wave_text_container {
  padding: 20px 0 0 0;
  display: flex;
  justify-content: center;
  gap: 10px;
  color: #fff;
}
</style>
