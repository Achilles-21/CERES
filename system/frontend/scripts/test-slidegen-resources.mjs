import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import ts from 'typescript'

const helperPath = path.resolve('src/utils/slideGenResources.ts')

if (!fs.existsSync(helperPath)) {
  throw new Error(`Missing helper: ${helperPath}`)
}

const source = fs.readFileSync(helperPath, 'utf-8')
const compiled = ts.transpileModule(source, {
  compilerOptions: {
    module: ts.ModuleKind.CommonJS,
    target: ts.ScriptTarget.ES2020
  }
}).outputText

const module = { exports: {} }
vm.runInNewContext(compiled, { exports: module.exports, module, console })

const { isFinalResourceStatus, isResourceReadyStatus, normalizeSlideImages } = module.exports
const toPlain = value => JSON.parse(JSON.stringify(value))

assert.equal(isResourceReadyStatus('配图匹配完成！共生成 4 张图片。正在生成PPT...'), true)
assert.equal(isResourceReadyStatus('PPT生成完成！您可以在资源面板中下载。'), true)
assert.equal(isResourceReadyStatus('Images matched. 4 images generated. Generating PPT...'), true)
assert.equal(isResourceReadyStatus('PPT generation complete. You can download it from Resources.'), true)
assert.equal(isResourceReadyStatus('Connected as user: 123'), false)
assert.equal(isFinalResourceStatus('PPT generation complete. You can download it from Resources.'), true)

assert.deepEqual(
  toPlain(normalizeSlideImages({
    images: [
      { image_id: 1, position: 'Section 1', query: 'retina anatomy' },
      null,
      { image_id: 2, position: 'Section 2', query: 'glaucoma' }
    ]
  })),
  [
    { image_id: 1, position: 'Section 1', query: 'retina anatomy' },
    { image_id: 2, position: 'Section 2', query: 'glaucoma' }
  ]
)

assert.deepEqual(toPlain(normalizeSlideImages({ images: null })), [])
assert.deepEqual(toPlain(normalizeSlideImages({})), [])

console.log('slideGen resource helpers passed')
