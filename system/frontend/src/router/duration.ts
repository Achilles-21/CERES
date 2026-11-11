import useSystemStore from '@/store/useSystemStore'
import { setDuration } from '@/api/system'

const exportObj = {
  timeout: null,  
  duration: 60 * 10,  
  event: false,  
  calc(from, to) {
    if(!this.event) this.handleEvent()
    const endPage = from.meta?.group
    const startPage = to.meta?.group

    useSystemStore().group = startPage
    if(endPage!=startPage && endPage) setDuration({type:endPage, event:0})
    if(endPage!=startPage && startPage) setDuration({type:startPage, event:1})


    clearTimeout(this.timeout)
    useSystemStore().showDurationLayout = false
    if(!startPage) return
    this.timeout = setTimeout(this.showLayout, this.duration * 1000)
  },
  handleEvent() {
    const eventList = ['click', 'dblclick', 'mousedown', 'mouseout', 'mouseover', 'mouseup', 'mousemove', 'onkeydown', 'onkeyup']

    for(let event of eventList) {
        document.addEventListener(event, () => {
          if(!useSystemStore().showDurationLayout && useSystemStore().group) {
            clearTimeout(this.timeout)
            this.timeout = setTimeout(this.showLayout, this.duration * 1000)
          }
        })
    }

    window.onbeforeunload = () => {
      if(useSystemStore().group) setDuration({type:useSystemStore().group, event:0})
    }

    this.event = true
  },
  showLayout() {
    clearTimeout(this.timeout)
    useSystemStore().showDurationLayout = true
    if(useSystemStore().group) setDuration({type:useSystemStore().group, event:0})
  },
  hideLayout() {
    useSystemStore().showDurationLayout = false
    if(useSystemStore().group) {
      setDuration({type:useSystemStore().group, event:1})
      this.timeout = setTimeout(this.showLayout, this.duration * 1000)
    }
  }
}

export default exportObj
