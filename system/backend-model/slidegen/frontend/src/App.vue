<template>
  <div class="app-container">
    <header v-if="isConnected" class="app-header">
      <div class="header-content">
        <img src="/avatars/logo.svg" alt="Ceres Logo" class="app-logo" />
        <div class="title-section">
          <h1>Ceres Slide Generation</h1>
          <p class="subtitle">多智能体眼科课件生成系统</p>
        </div>
      </div>
    </header>
    <main class="app-main">
      <ChatRoom @connection-changed="handleConnectionChange" />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatRoom from './components/ChatRoom.vue'

const isConnected = ref(false)

const handleConnectionChange = (connected) => {
  isConnected.value = connected
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.75rem 2rem;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.app-logo {
  width: 350px;
  height: 55px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.title-section {
  text-align: left;
}

.app-header h1 {
  font-size: 1.6rem;
  font-weight: 600;
}

.app-header .subtitle {
  font-size: 0.85rem;
  opacity: 0.9;
  margin-top: 0.15rem;
}

.app-main {
  flex: 1;
  overflow: hidden;
}
</style>
