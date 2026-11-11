<script lang="ts" setup>
import useSystemStore from '@/store/useSystemStore'
import LearningHeatmap from './components/LearningHeatmap.vue'
import { canUseSlideGen } from '@/utils/slideGenAccess'

const systemStore = useSystemStore()
const { proxy }: { proxy: CusProxy } = getCurrentInstance() as any

const currentLang = computed(() => String(systemStore.lang || "").toLowerCase())
const isEnglish = computed(() => currentLang.value.startsWith("en"))
const isMalay = computed(() => currentLang.value.includes("malay") || currentLang.value.startsWith("ms"))
const isHindi = computed(() => currentLang.value.includes("hindi") || currentLang.value.startsWith("hi"))
const isChinese = computed(() => currentLang.value.startsWith("zh"))
const titleSeparator = computed(() => (isChinese.value ? "，" : ", "))
const copy = computed(() => {
  if (isEnglish.value) {
    return {
      welcome: 'Welcome back',
      subtitle: 'Start a course, generate slides, practice, test, analyze cases, and join discussion from one focused workspace.',
      prompt: 'What would you like to learn today?',
      placeholder: 'Search a course, case, topic, or learning task',
      start: 'Start learning',
      quick: 'Quick entries',
      you: 'You',
      assistant: 'CERES assistant',
      thinking: 'Reading intent',
      intentError: 'I could not read that request. Please try again.',
      chatHint: 'Tell me where you want to go, or ask a study question directly.',
      recentLearning: 'Recent learning',
      recentNotes: 'Recent notes',
      recentDiscussion: 'Discussion updates',
      emptyLearning: 'No recent course progress yet. Open a course to build your learning timeline.',
      emptyNotes: 'Notes and reports will appear after you finish study sessions.',
      emptyDiscussion: 'Community replies and discussions will be shown here when available.',
      viewAll: 'View all',
      cards: {
        course: 'Continue structured video lessons with AI support.',
        slideGen: 'Create teaching slides from a topic or outline.',
        practice: 'Review knowledge with adaptive exercises.',
        test: 'Complete formal assessments and check readiness.',
        case: 'Work through guided clinical case analysis.',
        past: 'Review saved notes, PPT files, and study reports.',
        forum: 'Ask questions and follow course discussion.'
      },
      prompts: ['Continue my last course', 'Summarize today\'s notes']
    }
  }

  if (isMalay.value) {
    return {
      welcome: "Selamat kembali",
      subtitle: "Mulakan kursus, jana slaid, berlatih, menjalani ujian, menganalisis kes dan menyertai perbincangan dalam satu ruang kerja fokus.",
      prompt: "Apa yang ingin anda pelajari hari ini?",
      placeholder: "Cari kursus, kes, topik atau tugasan pembelajaran",
      start: "Mula belajar",
      quick: "Akses pantas",
      you: "Anda",
      assistant: "Pembantu CERES",
      thinking: "Membaca niat",
      intentError: "Permintaan ini tidak dapat dibaca. Sila cuba lagi.",
      chatHint: "Beritahu saya ke mana anda mahu pergi, atau terus tanya soalan pembelajaran.",
      recentLearning: "Pembelajaran terkini",
      recentNotes: "Nota terkini",
      recentDiscussion: "Kemas kini perbincangan",
      emptyLearning: "Belum ada kemajuan kursus terkini. Buka kursus untuk membina garis masa pembelajaran anda.",
      emptyNotes: "Nota dan laporan akan dipaparkan selepas anda menyelesaikan sesi pembelajaran.",
      emptyDiscussion: "Balasan komuniti dan perbincangan akan dipaparkan di sini apabila tersedia.",
      viewAll: "Lihat semua",
      cards: {
        course: "Teruskan pelajaran video berstruktur dengan sokongan AI.",
        slideGen: "Cipta slaid pengajaran daripada topik atau rangka.",
        practice: "Ulang kaji pengetahuan dengan latihan adaptif.",
        test: "Lengkapkan penilaian rasmi dan semak kesediaan.",
        case: "Selesaikan analisis kes klinikal berpandu.",
        past: "Semak nota, fail PPT dan laporan pembelajaran yang disimpan.",
        forum: "Ajukan soalan dan ikuti perbincangan kursus."
      },
      prompts: ["Teruskan kursus terakhir", "Ringkaskan nota hari ini"]
    }
  }

  if (isHindi.value) {
    return {
      welcome: "वापसी पर स्वागत है",
      subtitle: "एक केंद्रित कार्यक्षेत्र से कोर्स शुरू करें, स्लाइड बनाएं, अभ्यास करें, टेस्ट दें, केस का विश्लेषण करें और चर्चा में शामिल हों।",
      prompt: "आज आप क्या सीखना चाहेंगे?",
      placeholder: "कोर्स, केस, विषय या सीखने का कार्य खोजें",
      start: "सीखना शुरू करें",
      quick: "त्वरित प्रवेश",
      you: "आप",
      assistant: "CERES सहायक",
      thinking: "उद्देश्य पढ़ा जा रहा है",
      intentError: "यह अनुरोध पढ़ा नहीं जा सका। कृपया फिर से प्रयास करें।",
      chatHint: "बताएं कि आप कहां जाना चाहते हैं, या सीधे अध्ययन का प्रश्न पूछें।",
      recentLearning: "हाल की सीख",
      recentNotes: "हाल के नोट्स",
      recentDiscussion: "चर्चा अपडेट",
      emptyLearning: "अभी कोई हालिया कोर्स प्रगति नहीं है। अपनी सीखने की समयरेखा बनाने के लिए कोई कोर्स खोलें।",
      emptyNotes: "अध्ययन सत्र पूरा करने के बाद नोट्स और रिपोर्ट यहां दिखाई देंगे।",
      emptyDiscussion: "उपलब्ध होने पर समुदाय के उत्तर और चर्चाएं यहां दिखाई देंगी।",
      viewAll: "सभी देखें",
      cards: {
        course: "AI सहायता के साथ संरचित वीडियो पाठ जारी रखें।",
        slideGen: "किसी विषय या रूपरेखा से शिक्षण स्लाइड बनाएं।",
        practice: "अनुकूल अभ्यासों से ज्ञान की समीक्षा करें।",
        test: "औपचारिक मूल्यांकन पूरा करें और तैयारी जांचें।",
        case: "निर्देशित क्लिनिकल केस विश्लेषण पूरा करें।",
        past: "सहेजे गए नोट्स, PPT फाइलें और अध्ययन रिपोर्ट देखें।",
        forum: "प्रश्न पूछें और कोर्स चर्चा का अनुसरण करें।"
      },
      prompts: ["पिछला कोर्स जारी रखें", "आज के नोट्स सारांशित करें"]
    }
  }

  return {
    welcome: '欢迎回来',
    subtitle: '从一个工作台进入课程、课件生成、练习、考试、病例分析和讨论，保持学习任务连续。',
    prompt: '今天想学习什么？',
    placeholder: '搜索课程、病例、主题或学习任务',
    start: '开始学习',
    quick: '快捷入口',
    you: '你',
    assistant: 'CERES 助手',
    thinking: '正在分析意图',
    intentError: '暂时无法识别这个请求，请稍后再试。',
    chatHint: '告诉我你想去哪个学习模块，也可以直接问我一个学习问题。',
    recentLearning: '最近学习',
    recentNotes: '最近笔记',
    recentDiscussion: '讨论动态',
    emptyLearning: '暂无最近课程进度，进入课程后会在这里形成学习时间线。',
    emptyNotes: '完成学习后，个人笔记、PPT 和学习报告会在这里聚合。',
    emptyDiscussion: '课程讨论与回复动态会在这里展示。',
    viewAll: '查看全部',
    cards: {
      course: '继续视频课程，进入 AI 辅助学习流程。',
      slideGen: '根据主题或大纲生成专业课件。',
      practice: '用练习巩固知识点和课程节点。',
      test: '进入考试测评，检查当前掌握情况。',
      case: '完成分步病例分析与问答推理。',
      past: '回看个人笔记、PPT 与学习报告。',
      forum: '参与课程讨论，查看问题回复。'
    },
    prompts: ['继续上次课程', '整理今日笔记']
  }
})

const displayName = computed(() => systemStore.userInfo?.nickName || systemStore.userInfo?.userName || proxy.$t('home.studySystem'))

const launchActions = computed(() => [
  {
    title: proxy.$t('home.goToClass'),
    description: copy.value.cards.course,
    path: '/course',
    color: '#2563eb',
    icon: 'iconify solar--monitor-bold'
  },
  ...(canUseSlideGen(systemStore.userInfo)
    ? [
        {
          title: proxy.$t('home.slideGen'),
          description: copy.value.cards.slideGen,
          path: '/slideGen',
          color: '#8b47ea',
          icon: 'iconify ic--outline-slideshow'
        }
      ]
    : []),
  {
    title: proxy.$t('home.goPractice'),
    description: copy.value.cards.practice,
    path: '/exam',
    color: '#059669',
    icon: 'iconify ic--outline-attachment'
  },
  {
    title: proxy.$t('home.goTest'),
    description: copy.value.cards.test,
    path: '/testCate',
    color: '#0891b2',
    icon: 'iconify ic--outline-insert-invitation'
  },
  {
    title: proxy.$t('home.PreviousNotesAndReports'),
    description: copy.value.cards.past,
    path: '/pastlist',
    color: '#4f46e5',
    icon: 'iconify ic--outline-library-books'
  },
  {
    title: proxy.$t('home.discussion'),
    description: copy.value.cards.forum,
    path: '/forum',
    color: '#0d9488',
    icon: 'iconify ic--baseline-co-present'
  }
])


type HomeChatMessage = {
  id: number
  role: 'user' | 'assistant'
  text: string
  loading?: boolean
  error?: boolean
}

type HomeIntentResult = {
  reply: string
  shouldRoute: boolean
  route: string
}

const promptValue = ref('')
const chatMessages = ref<HomeChatMessage[]>([])
const promptLoading = ref(false)
const chatTranscriptRef = ref<HTMLElement | null>(null)

let chatMessageId = 0

const availableIntentRoutes = computed(() =>
  launchActions.value.map(({ title, description, path }) => ({
    title,
    description,
    path
  }))
)

const availableIntentRoutePaths = computed(() => availableIntentRoutes.value.map(item => item.path))

const getModelApiUrl = (path: string) => {
  const base = String(import.meta.env.VITE_MODEL_API_URL || '/model-api').replace(/\/+$/, '') || '/model-api'
  return `${base}${path.startsWith('/') ? path : `/${path}`}`
}

const getNextChatMessageId = () => {
  chatMessageId += 1
  return chatMessageId
}

const scrollChatToEnd = async () => {
  await nextTick()
  if (chatTranscriptRef.value) {
    chatTranscriptRef.value.scrollTop = chatTranscriptRef.value.scrollHeight
  }
}

const delay = (ms: number) => new Promise(resolve => window.setTimeout(resolve, ms))

const readIntentPayload = (payload: any): HomeIntentResult => {
  const data = payload?.data && typeof payload.data === 'object' ? payload.data : payload
  return {
    reply: typeof data?.reply === 'string' ? data.reply : typeof data?.message === 'string' ? data.message : '',
    shouldRoute: Boolean(data?.should_route ?? data?.shouldRoute),
    route: typeof data?.route === 'string' ? data.route : ''
  }
}

const postHomeIntent = async (text: string): Promise<HomeIntentResult> => {
  const response = await fetch(getModelApiUrl('/home/intent'), {
    method: 'POST',
    credentials: 'include',
    headers: {
      Authorization: localStorage.getItem('token') || '',
      'Content-Type': 'application/json;charset=UTF-8'
    },
    body: JSON.stringify({
      user_id: String(systemStore.userInfo?.userId ?? systemStore.userInfo?.id ?? 'home'),
      time_span: new Date().toISOString(),
      input_text: text,
      role_id: String(systemStore.userInfo?.roleId ?? ''),
      level: String(systemStore.userInfo?.level ?? ''),
      language: String(systemStore.lang || '')
    })
  })

  const responseText = await response.text()
  let payload: any = {}

  if (responseText) {
    try {
      payload = JSON.parse(responseText)
    } catch {
      payload = { message: responseText }
    }
  }

  if (!response.ok) {
    throw new Error(payload?.msg || payload?.message || 'Intent request failed')
  }

  return readIntentPayload(payload)
}

const getRouteBasePath = (route: string) => route.split(/[?#]/)[0]

const isAllowedHomeRoute = (route: string) => {
  const cleanRoute = route.trim()
  if (!cleanRoute.startsWith('/') || cleanRoute.startsWith('//')) return false
  return availableIntentRoutePaths.value.includes(getRouteBasePath(cleanRoute))
}

const updateAssistantMessage = (id: number, updates: Partial<HomeChatMessage>) => {
  chatMessages.value = chatMessages.value.map(message => (message.id === id ? { ...message, ...updates } : message))
}

const toUrl = (url: string) => {
  if (/^https?:\/\//.test(url)) {
    window.open(url, '_blank')
    return
  }
  proxy.$router.push(url)
}

const submitPrompt = async (quickText?: string) => {
  const text = String(quickText ?? promptValue.value).trim()
  if (!text || promptLoading.value) return

  promptValue.value = ''
  promptLoading.value = true

  const userMessageId = getNextChatMessageId()
  const assistantMessageId = getNextChatMessageId()
  chatMessages.value = [
    ...chatMessages.value,
    { id: userMessageId, role: 'user', text },
    { id: assistantMessageId, role: 'assistant', text: '', loading: true }
  ]
  await scrollChatToEnd()

  try {
    const intent = await postHomeIntent(text)
    const reply = intent.reply.trim() || copy.value.intentError
    updateAssistantMessage(assistantMessageId, { text: reply, loading: false })
    await scrollChatToEnd()

    const route = intent.route.trim()
    if (intent.shouldRoute && route && isAllowedHomeRoute(route)) {
      await delay(900)
      proxy.$router.push(route)
    }
  } catch {
    updateAssistantMessage(assistantMessageId, { text: copy.value.intentError, loading: false, error: true })
    await scrollChatToEnd()
  } finally {
    promptLoading.value = false
  }
}
</script>

<template>
  <main class="home-launchpad ceres-gradient-bg" :class="{ 'is-dark': systemStore.theme }">
    <section class="home-shell ceres-page-shell">
      <div class="home-hero">
        <div class="home-intro" aria-labelledby="home-welcome-title">
          <div class="teacher-portrait">
            <img class="teacher-illustration" src="/professor.svg" alt="Cartoon male teacher with glasses / 戴眼镜的男性卡通教师" />
          </div>
          <div class="home-copy">
            <div class="home-eyebrow ceres-pill">
              <span>{{ $t('home.studySystem') }}</span>
            </div>
            <h1 id="home-welcome-title">{{ copy.welcome }}{{ titleSeparator }}{{ displayName }}</h1>
            <p>{{ copy.subtitle }}</p>
          </div>
        </div>

        <section class="prompt-card chat-card ceres-card ceres-glass" aria-labelledby="home-task-title">
          <div class="chat-card-header">
            <h2 id="home-task-title">{{ copy.prompt }}</h2>
          </div>

          <div ref="chatTranscriptRef" class="chat-transcript" role="log" aria-live="polite" aria-relevant="additions text">
            <div v-if="!chatMessages.length" class="chat-row is-assistant">
              <div class="chat-bubble">{{ copy.chatHint }}</div>
            </div>
            <div
              v-for="message in chatMessages"
              :key="message.id"
              class="chat-row"
              :class="[`is-${message.role}`, { 'is-loading': message.loading, 'is-error': message.error }]"
            >
              <div class="chat-bubble">
                <span class="sr-only">{{ message.role === 'user' ? copy.you : copy.assistant }}: </span>
                <template v-if="message.loading">
                  <span class="loading-dots" aria-hidden="true">
                    <span></span>
                    <span></span>
                    <span></span>
                  </span>
                  <span class="sr-only">{{ copy.thinking }}</span>
                </template>
                <template v-else>{{ message.text }}</template>
              </div>
            </div>
          </div>

          <form class="chat-composer" @submit.prevent="submitPrompt()">
            <label class="sr-only" for="home-task-input">{{ copy.prompt }}</label>
            <div class="prompt-input-wrap">
              <span class="iconify solar--magnifer-linear prompt-search-icon" aria-hidden="true"></span>
              <input id="home-task-input" v-model="promptValue" :placeholder="copy.placeholder" type="text" inputmode="text" autocomplete="off" />
              <button class="prompt-submit ceres-icon-button" type="submit" :disabled="promptLoading || !promptValue.trim()" :aria-label="copy.start" :title="copy.start">
                <span class="iconify solar--arrow-right-linear" aria-hidden="true"></span>
              </button>
            </div>
          </form>

          <div class="quick-pills" :aria-label="copy.quick">
            <button v-for="item in copy.prompts" :key="item" class="quick-pill ceres-pill" type="button" :disabled="promptLoading" @click="submitPrompt(item)">
              {{ item }}
            </button>
          </div>
        </section>
      </div>

      <LearningHeatmap />

      <section class="section-block" :aria-label="copy.quick">
        <div class="section-heading">
          <h2>{{ copy.quick }}</h2>
          <span class="section-line"></span>
        </div>
        <div class="feature-grid">
          <button
            v-for="item in launchActions"
            :key="item.path"
            class="feature-card ceres-card"
            type="button"
            :style="{ '--accent-color': item.color }"
            @click="toUrl(item.path)"
          >
            <span class="feature-icon" aria-hidden="true">
              <span :class="item.icon"></span>
            </span>
            <span class="feature-content">
              <span class="feature-title">{{ item.title }}</span>
              <span class="feature-description">{{ item.description }}</span>
            </span>
            <span class="feature-arrow iconify solar--arrow-right-up-linear" aria-hidden="true"></span>
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<style lang="css" scoped>
.home-launchpad {
  min-height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  color: #0f172a;
}

.home-shell {
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: clamp(20px, 3vw, 40px);
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 460px);
  gap: clamp(18px, 3vw, 32px);
  align-items: stretch;
  animation: fade-up 260ms ease both;
}

.home-intro {
  display: grid;
  min-width: 0;
  grid-template-columns: minmax(130px, 190px) minmax(0, 1fr);
  align-items: center;
  gap: clamp(16px, 3vw, 30px);
}

.teacher-portrait {
  position: relative;
  display: flex;
  width: min(100%, 190px);
  min-width: 0;
  min-height: 282px;
  align-items: flex-end;
  justify-content: center;
  justify-self: center;
  overflow: hidden;
}

.teacher-portrait::after {
  position: absolute;
  right: 12%;
  bottom: 12px;
  left: 12%;
  height: 16px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.14);
  content: "";
  filter: blur(5px);
}

.teacher-illustration {
  position: relative;
  z-index: 1;
  display: block;
  width: min(100%, 178px);
  height: 276px;
  object-fit: cover;
  object-position: top center;
  transform-origin: 50% 86%;
  animation: teacher-sway 6.4s ease-in-out infinite;
  filter: drop-shadow(0 18px 24px rgba(48, 28, 96, 0.16));
  will-change: transform;
}

.home-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
}

.home-eyebrow {
  display: inline-flex;
  width: fit-content;
  min-width: 128px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-inline: 18px;
  border: 1px solid rgba(114, 46, 209, 0.22);
  background: rgba(255, 255, 255, 0.78);
  color: #5b21b6;
  font-weight: 700;
  white-space: nowrap;
}

.home-intro h1 {
  margin: 0;
  color: #111827;
  font-size: clamp(30px, 4.5vw, 54px);
  font-weight: 800;
  line-height: 1.08;
}

.home-intro p {
  max-width: 620px;
  margin: 0;
  color: #475569;
  font-size: clamp(16px, 2vw, 19px);
  line-height: 1.75;
}

.prompt-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
  padding: clamp(18px, 2.4vw, 26px);
}

.chat-card {
  min-height: 352px;
  justify-content: space-between;
}

.chat-card-header {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chat-card-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.35;
}

.chat-transcript {
  display: flex;
  min-height: 144px;
  max-height: 260px;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding: 2px;
  scrollbar-gutter: stable;
}

.chat-empty-slot {
  width: min(82%, 310px);
  min-height: 74px;
  border: 1px solid rgba(114, 46, 209, 0.14);
  border-radius: 18px 18px 18px 6px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.72), rgba(238, 242, 255, 0.62));
  box-shadow: 0 12px 26px rgba(79, 70, 229, 0.08);
}

.chat-row {
  display: flex;
  min-width: 0;
  align-items: flex-end;
}

.chat-row.is-user {
  justify-content: flex-end;
}

.chat-row.is-assistant {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: min(86%, 330px);
  padding: 12px 14px;
  border-radius: 18px;
  font-size: 14.5px;
  line-height: 1.6;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-row.is-assistant .chat-bubble {
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-bottom-left-radius: 6px;
  background: rgba(255, 255, 255, 0.9);
  color: #1f2937;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.chat-row.is-user .chat-bubble {
  border-bottom-right-radius: 6px;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #ffffff;
  box-shadow: 0 12px 26px rgba(114, 46, 209, 0.24);
}

.chat-row.is-error .chat-bubble {
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(254, 242, 242, 0.94);
  color: #991b1b;
}

.loading-dots {
  display: inline-flex;
  min-width: 38px;
  height: 20px;
  align-items: center;
  gap: 5px;
  color: #722ed1;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  animation: loading-dot 1s ease-in-out infinite;
  background: currentColor;
  opacity: 0.45;
}

.loading-dots span:nth-child(2) {
  animation-delay: 120ms;
}

.loading-dots span:nth-child(3) {
  animation-delay: 240ms;
}

.chat-composer {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.prompt-input-wrap {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 44px;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(114, 46, 209, 0.2);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 10px 10px 14px;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.prompt-input-wrap:focus-within {
  border-color: rgba(114, 46, 209, 0.75);
  box-shadow: 0 0 0 4px rgba(114, 46, 209, 0.13);
}

.prompt-search-icon {
  color: #722ed1;
  font-size: 22px;
}

.prompt-input-wrap input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #0f172a;
  font-size: 16px;
}

.prompt-input-wrap input::placeholder {
  color: #64748b;
}

.prompt-submit {
  display: inline-flex;
  width: 44px;
  height: 44px;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #722ed1, #8b47ea);
  color: #fff;
  font-size: 22px;
  transition: box-shadow 180ms ease, transform 180ms ease, filter 180ms ease;
}

.prompt-submit:hover {
  filter: brightness(1.04);
  box-shadow: 0 16px 30px rgba(114, 46, 209, 0.28);
  transform: translateY(-1px);
}

.prompt-submit:disabled,
.quick-pill:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.prompt-submit:disabled,
.prompt-submit:disabled:hover {
  filter: saturate(0.85);
  box-shadow: none;
  transform: none;
}

.quick-pill:disabled,
.quick-pill:disabled:hover {
  border-color: rgba(79, 70, 229, 0.18);
  background: rgba(255, 255, 255, 0.62);
  color: #4338ca;
  box-shadow: none;
}

.quick-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-pill {
  cursor: pointer;
  border: 1px solid rgba(79, 70, 229, 0.18);
  background: rgba(255, 255, 255, 0.78);
  color: #4338ca;
  font-weight: 700;
  transition: border-color 180ms ease, background 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.quick-pill {
  min-width: min(100%, 168px);
  max-width: 100%;
  padding: 0 20px;
  text-align: center;
  white-space: normal;
}

.quick-pill:hover {
  border-color: rgba(114, 46, 209, 0.48);
  background: rgba(114, 46, 209, 0.08);
  color: #5b21b6;
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.12);
}

.section-block {
  margin-top: clamp(22px, 3vw, 34px);
  animation: fade-up 280ms ease both;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.section-heading h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 800;
}

.section-line {
  height: 1px;
  flex: 1;
  background: linear-gradient(90deg, rgba(114, 46, 209, 0.28), rgba(8, 145, 178, 0));
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.feature-card {
  position: relative;
  display: grid;
  min-width: 0;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 14px;
  min-height: 150px;
  cursor: pointer;
  border: 1px solid rgba(148, 163, 184, 0.26);
  padding: 18px;
  text-align: left;
  transition: border-color 180ms ease, box-shadow 200ms ease, transform 200ms ease, background 180ms ease;
}

.feature-card:hover {
  border-color: color-mix(in srgb, var(--accent-color) 58%, transparent);
  box-shadow: 0 20px 42px rgba(30, 41, 59, 0.12);
  transform: translateY(-2px);
}

.feature-card:hover .feature-arrow,
.feature-card:focus-visible .feature-arrow {
  opacity: 1;
  transform: translate(0, 0);
}

.feature-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: color-mix(in srgb, var(--accent-color) 13%, white);
  color: var(--accent-color);
  font-size: 24px;
}

.feature-content {
  display: flex;
  min-width: 0;
}

.feature-content {
  flex-direction: column;
  gap: 10px;
}


.feature-title {
  color: #111827;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.35;
}


.feature-description {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
}

.feature-arrow {
  position: absolute;
  top: 18px;
  right: 18px;
  color: var(--accent-color);
  font-size: 20px;
  opacity: 0;
  transform: translate(-4px, 4px);
  transition: opacity 180ms ease, transform 180ms ease;
}


.home-launchpad.is-dark {
  color: #f8fafc;
}

.home-launchpad.is-dark .home-eyebrow,
.home-launchpad.is-dark .quick-pill {
  border-color: rgba(196, 181, 253, 0.28);
  background: rgba(139, 71, 234, 0.2);
  color: #f8fafc;
}

.home-launchpad.is-dark .home-intro h1,
.home-launchpad.is-dark .home-intro p,
.home-launchpad.is-dark .chat-card-header h2,
.home-launchpad.is-dark .section-heading h2,
.home-launchpad.is-dark .feature-title,
.home-launchpad.is-dark .home-copy span,
.home-launchpad.is-dark .quick-pill,
.home-launchpad.is-dark .feature-content span {
  color: #f8fafc;
}

.home-launchpad.is-dark .home-intro p,
.home-launchpad.is-dark .feature-description {
  color: #e2e8f0;
}

.home-launchpad.is-dark .prompt-card,
.home-launchpad.is-dark .feature-card {
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(23, 24, 42, 0.78);
}

.home-launchpad.is-dark .prompt-input-wrap {
  border-color: rgba(196, 181, 253, 0.24);
  background: rgba(15, 23, 42, 0.68);
}

.home-launchpad.is-dark .prompt-input-wrap input {
  color: #ffffff;
}

.home-launchpad.is-dark .prompt-input-wrap input::placeholder {
  color: #dbeafe;
}

.home-launchpad.is-dark .chat-empty-slot {
  border-color: rgba(196, 181, 253, 0.16);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.62), rgba(49, 46, 129, 0.3));
  box-shadow: none;
}

.home-launchpad.is-dark .chat-row.is-assistant .chat-bubble {
  border-color: rgba(196, 181, 253, 0.18);
  background: rgba(15, 23, 42, 0.72);
  color: #f8fafc;
  box-shadow: none;
}

.home-launchpad.is-dark .chat-row.is-error .chat-bubble {
  border-color: rgba(248, 113, 113, 0.34);
  background: rgba(127, 29, 29, 0.38);
  color: #fee2e2;
}

.home-launchpad.is-dark .loading-dots {
  color: #c4b5fd;
}

.home-launchpad.is-dark .prompt-search-icon,
.home-launchpad.is-dark .feature-arrow {
  color: #c4b5fd;
}

.home-launchpad.is-dark .feature-icon {
  background: rgba(196, 181, 253, 0.16);
  color: #ddd6fe;
}

.home-launchpad.is-dark .quick-pill:hover {
  border-color: rgba(196, 181, 253, 0.44);
  background: rgba(139, 71, 234, 0.3);
  color: #ffffff;
}

.prompt-submit:focus-visible,
.quick-pill:focus-visible,
.feature-card:focus-visible {
  outline: 3px solid rgba(114, 46, 209, 0.32);
  outline-offset: 3px;
}

@keyframes teacher-sway {
  0%,
  100% {
    transform: rotate(-2.4deg) translateX(-2px);
  }

  50% {
    transform: rotate(2.4deg) translateX(2px);
  }
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes loading-dot {
  0%,
  80%,
  100% {
    opacity: 0.45;
    transform: translateY(0);
  }

  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@media (max-width: 900px) {
  .home-hero {
    grid-template-columns: 1fr;
  }

  .home-intro {
    grid-template-columns: minmax(124px, 172px) minmax(0, 1fr);
  }

  .teacher-portrait {
    width: min(100%, 172px);
  }

  .teacher-illustration {
    width: min(100%, 162px);
  }
}

@media (max-width: 640px) {
  .home-shell {
    padding: 18px 14px 28px;
  }

  .home-intro {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
  }

  .teacher-portrait {
    width: min(100%, 170px);
    min-height: 214px;
  }

  .teacher-illustration {
    width: min(100%, 150px);
    height: 210px;
  }

  .home-copy {
    align-items: center;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-card {
    min-height: auto;
  }

  .chat-card {
    min-height: 330px;
  }

  .chat-transcript {
    min-height: 128px;
    max-height: 240px;
  }

  .chat-bubble {
    max-width: 92%;
  }

  .quick-pill {
    flex: 1 1 calc(50% - 10px);
    min-width: min(100%, 144px);
  }

  .prompt-input-wrap {
    grid-template-columns: 24px minmax(0, 1fr) 40px;
  }

  .prompt-submit {
    width: 40px;
    height: 40px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-launchpad *,
  .home-launchpad *::before,
  .home-launchpad *::after {
    animation: none !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
