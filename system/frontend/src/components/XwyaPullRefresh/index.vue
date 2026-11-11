<template>
  <div class="w-full h-full bg-white overflow-hidden">
    <div ref="pullBox" class=" text-center  relative left-0  top-[-50px]  "
      :style="{ height: DEFAULT_HEAD_HEIGHT + 'px', lineHeight: DEFAULT_HEAD_HEIGHT + 'px', transition: 'all 0.3s linear' }">
      {{ stateStore[pullState.status] }}
    </div>
    <div ref="mainBox" @touchstart.passive="onTouchStart" @touchend="onTouchEnd" @touchmove="onTouchmove"
      @touchcancel="onTouchEnd" :style="{ transition: 'all 0.3s linear' }"
      class=" relative left-0 top-[-50px] h-full bg-white overflow-y-auto overflow-hidden ">
      <slot></slot>
      <div class="text-center" v-if="loading">加载中...</div>
      <div class="text-center" v-if="!finished">没有更多了...</div>
    </div>
  </div>
</template>

<script setup lang="ts">

const DEFAULT_HEAD_HEIGHT = 50;
const stateStore = [" ", "下拉即可刷新", "释放即可刷新", "重新加载中..."]
const props = defineProps({
  load: {
    type: Function,
    default: () => { }
  },
  refresh: {
    type: Function,
    default: () => { }
  },
  loading: {
    type: Boolean,
    default: false
  },
  finished: {
    type: Boolean,
    default: true
  }

})
const pullState = reactive({
  isPull: true,
  status: 0,
  headHeight: DEFAULT_HEAD_HEIGHT,
  startY: 0
})
const pullBox = ref(null)
const mainBox = ref(null)
const timer = ref(null)
const hasMoreData = ref(true)
const onTouchStart = (e: TouchEvent) => {
  if (!pullState.isPull) return;
  pullState.startY = e.changedTouches[0].clientY
}
const onTouchEnd = async (e: TouchEvent) => {
  if (!pullState.isPull) return;
  pullState.startY = e.changedTouches[0].clientY - pullState.startY
  if (pullState.startY - 0.5 * DEFAULT_HEAD_HEIGHT >= DEFAULT_HEAD_HEIGHT) {
    pullState.status = 3
    pullBox.value.style.top = "0px"
    pullBox.value.style.transform = `translateY(0px)`;
    mainBox.value.style.transform = "translateY(50px)"
    await props.refresh()
    mainBox.value.style.transform = "translateY(0px)"
    pullBox.value.style.top = ""
  } else {
    mainBox.value.style.transform = "translateY(0px)"
    pullBox.value.style.transform = `translateY(0px)`;
  }

}
const onTouchmove = (e) => {

  if (!pullState.isPull) return;
  const distance = e.changedTouches[0].clientY - pullState.startY;
  let t = distance
  if (distance > 1.5 * DEFAULT_HEAD_HEIGHT) {
    pullState.status = 2;  
    t = distance > 3 * DEFAULT_HEAD_HEIGHT ? 3 * DEFAULT_HEAD_HEIGHT : distance
  } else {
    pullState.status = 1;  
  }
  pullBox.value.style.transform = `translateY(${t}px)`;
  mainBox.value.style.transform = `translateY(${t}px)`;
}

const onMainScroll = () => {
  pullState.isPull = mainBox.value.scrollTop > 0 ? false : true
  if (mainBox.value.scrollTop + mainBox.value.clientHeight >= mainBox.value.scrollHeight && props.finished) {
    if (timer.value) return;
    timer.value = setTimeout(async () => {
      await props.load()
      timer.value = null;
    }, 100);
  }
}
const checkHasMoreData = () => {
  nextTick(() => {
    const containerHeight = mainBox.value.clientHeight;
    const contentHeight = mainBox.value.scrollHeight;
    if (contentHeight <= containerHeight) {
      initload()
    }
  });
}

const initload = () => {

  props.load().then(() => {
    if (props.finished) {
      checkHasMoreData()
    }
  })
}
onMounted(() => {
  mainBox.value.addEventListener('scroll', onMainScroll)
  initload()
})
onBeforeUnmount(() => {
  mainBox.value.removeEventListener('scroll', onMainScroll)
})

</script>

<style lang="css" scoped>
.a {
  overflow-y: auto;
}
</style>
