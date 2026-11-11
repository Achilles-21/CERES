<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import useSystemStore from '@/store/useSystemStore'
import duration from '@/router/duration'
const {proxy} = getCurrentInstance()

const offlineStoreForSetup = useSystemStore()
const { showDurationLayout } = storeToRefs(offlineStoreForSetup)

const changeOffline = () => {
  console.log('++++++++++++++++++++++')
  duration.hideLayout()
}

</script>

<template>
  <div class="offline-layout" v-if="showDurationLayout">
    <div class="layout">
      <label>{{proxy.$t('duration.title')}}</label>
      <label>{{proxy.$t('duration.tip')}}</label>
      <div>
        <span @click="changeOffline">{{proxy.$t('duration.button')}}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.offline-layout{position:absolute;top:0;left:0;bottom:0;right:0;display:flex;align-items:center;background:rgba(0,0,0,.6);z-index:9999999;justify-content:center;}
.offline-layout .layout{padding:20px;border-radius:6px;background:#fff;width:280px;}
.offline-layout .layout label:nth-child(1){font-size:18px;font-weight:600;color:#333;}
.offline-layout .layout label:nth-child(2){font-size:12px;color:#999;display:block;}
.offline-layout .layout div{text-align:right;margin-top:6px;}
.offline-layout .layout div span{display:inline-block;background:#000;color:#fff;padding:4px 10px;border-radius:6px;cursor:pointer;}
</style>
