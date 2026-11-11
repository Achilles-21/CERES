<template>
  <div class="">
    chart

    <n-button type="primary" @click="ddd">发送1 </n-button>
    <n-button type="primary" @click="dddd">发送2 </n-button>
    <n-button type="primary" @click="close">关闭 </n-button>
  </div>
</template>

<script setup lang="ts">
let socket =  new WebSocket(import.meta.env.VITE_CHART_WS_URL || `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/chart-ws`)
const initSokect = () => {
socket.addEventListener('open', (event) => {
  console.log('WebSocket is open now.');
});
socket.addEventListener('message', (event) => {
  console.log('Message from server ', event.data);
});
}
const ddd = () => {
socket.send('我是1');
}
const dddd = () => {
  socket.send({id:1,aaa:"2222"});
}

const close = () => {
  socket.close()
}
onMounted(() => {
  initSokect()
})
onBeforeUnmount(() => {
socket.close();

})

</script>

<style lang="scss" scoped></style>
