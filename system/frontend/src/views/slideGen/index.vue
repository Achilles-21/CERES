<script lang="ts" setup>
</script>

<template>
  <main class="slidegen-page ceres-gradient-bg">
    <ChatRoom />
  </main>
</template>

<style scoped>
.slidegen-page {
  min-height: calc(100vh - 64px);
  height: calc(100vh - 64px);
  overflow: hidden;
  background:
    radial-gradient(circle at 8% 8%, rgba(114, 46, 209, 0.14), transparent 30%),
    radial-gradient(circle at 88% 14%, rgba(6, 182, 212, 0.12), transparent 28%),
    linear-gradient(135deg, #f8f5ff 0%, #f7fbff 48%, #f4fff9 100%);
}
</style>
