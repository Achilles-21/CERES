import { IButtonMenu, IDomEditor, Boot } from '@wangeditor/editor'
import type { IButtonMenu } from "@wangeditor/editor"
import RegionScreenshot from "region-screenshot-js";
class VideoScreenshot implements IButtonMenu {
  constructor() {
    this.title = '截图'  
    this.iconSvg = `<svg t="1721103089752" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4394" xmlns:xlink="http://www.w3.org/1999/xlink" width="200" height="200"><path d="M981.3 821.3h-96V170.7c0-17.7-14.3-32-32-32H202.7v-96c0-17.7-14.3-32-32-32s-32 14.3-32 32v96h-96c-17.7 0-32 14.3-32 32s14.3 32 32 32h96v650.7c0 17.7 14.3 32 32 32h650.7v96c0 17.7 14.3 32 32 32s32-14.3 32-32v-96h96c17.7 0 32-14.3 32-32-0.1-17.7-14.4-32.1-32.1-32.1z m-778.6 0V202.7h618.7v618.7H202.7z" p-id="4395"></path></svg>`  
    this.tag = 'button'
  }
  getValue(editor: IDomEditor): string | boolean {
    return false
  }

  isActive(editor: IDomEditor): boolean {
    return false
  }

  isDisabled(editor: IDomEditor): boolean {
    return false
  }

  exec(editor: IDomEditor, value: string | boolean) {
    if (this.isDisabled(editor)) return
    let screenshot = new RegionScreenshot();
    screenshot.on("screenshotGenerated", (dataUrl) => {

      const imageNode = {
        type: 'image',  
        src: dataUrl,  
        alt: 'Screenshot',  
        style: { maxWidth: '100%', height: 'auto' },  
        children: [{ text: '' }]
      };
      editor.insertNode(imageNode);

    })
    screenshot.on('errorCreated', (res) => {
        console.log(res, '@@@@@@@@@@22222')
    })



  }
}
const menu1Conf:IButtonMenu = {
  key: 'videoScreenshot',  
  factory() {
    return new VideoScreenshot()  
  },
}

Boot.registerMenu(menu1Conf)



