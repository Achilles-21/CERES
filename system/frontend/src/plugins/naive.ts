import { createDiscreteApi } from 'naive-ui'
const { message, loadingBar,notification,dialog,modal } = createDiscreteApi(['message', 'loadingBar',"notification","dialog",'modal'])


window.$msg = message
window.$bar = loadingBar
window.$notice = notification
window.$dialog = dialog
window.$modal = modal
