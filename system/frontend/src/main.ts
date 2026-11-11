import { createApp } from 'vue'
import './style.css'
import 'video.js/dist/video-js.css';

import init from "@/plugins"
import App from './App.vue'
const app = createApp(App)
init(app)
app.mount('#app')
