<template>
   <component :is="layout[systemStore.layout]" />
</template>

<script setup lang="ts">
import vertical from "./vertical.vue"
import level from "./level.vue";
import useSystemStore from "@/store/useSystemStore";
const layout = { vertical,level}
const systemStore = useSystemStore()


</script>

<style  scoped>

</style>
