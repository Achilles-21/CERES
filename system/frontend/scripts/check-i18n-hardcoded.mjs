import { readFileSync } from 'node:fs'

const files = [
  'src/components/SlideGen/ChatRoom.vue',
  'src/components/SlideGen/UserInput.vue',
  'src/components/SlideGen/AgentMessage.vue',
  'src/views/viewppt/index.vue',
  'src/views/report/index.vue',
  'src/layouts/down.vue',
  'src/components/XwyaLayout/index.vue',
  'src/views/case/index.vue',
  'src/views/forum/index.vue',
  'src/views/news/index.vue',
  'src/views/postWritings/index.vue',
  'src/components/ResponsiveModal/index.vue',
  'src/components/NewsModal/index.vue',
]

const allowedFragments = [
  "hasMessage(['配图'",
  "'图片', 'image'",
  "'撰写', '内容'",
  "'大纲', 'outline'",
  "data.content.includes('配图匹配完成')",
  "{ label: '简体中文', key: 'zhcn' }",
  "roleName === '前台用户'",
]

const cjkPattern = /[\u4e00-\u9fff]/
const violations = []

for (const file of files) {
  const content = readFileSync(new URL(`../${file}`, import.meta.url), 'utf8')
  content.split(/\r?\n/).forEach((line, index) => {
    if (!cjkPattern.test(line)) return
    if (allowedFragments.some(fragment => line.includes(fragment))) return
    violations.push(`${file}:${index + 1}: ${line.trim()}`)
  })
}

if (violations.length) {
  console.error('Hardcoded CJK UI strings found in i18n-managed files:')
  console.error(violations.join('\n'))
  process.exit(1)
}
