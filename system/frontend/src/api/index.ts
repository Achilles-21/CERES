let apiall = {}
const modules = import.meta.glob<{ [key: string]: any }>('./*.ts', { eager: true })
for (const path in modules) {
  const name = path.split(/[\/\.]/)[2] as keyof Api
   apiall[name]=modules[path]
}
export default apiall as any
