import { createApp } from "vue"
import XwyaWave from "@/components/XwyaWave"
let app = null
let thea = null
let el = document.createElement('div')
class Media {
  mediaRecorder: MediaRecorder | null = null;
  errorHandle: ((err: Error) => void) | null = null;
  successHandle: ((data: Blob, url: string) => void) | null = null;
  audioChunks: BlobPart[] = [];
  audioContext: AudioContext | null = null;
  analyser: AnalyserNode | null = null;
  source: MediaStreamAudioSourceNode | null = null;
  isVisualizing: boolean = false;  
  falg : boolean = false
  constructor() {
    thea=this
  }

  async ensureStream(): Promise<boolean> {
    if (this.mediaRecorder) return true
    try {
      const stream: MediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(stream);
      this.onDataAvailable();
      this.setupAudioProcessing(stream);
      document.body.appendChild(el);
      return true
    } catch (err) {
      console.log("err", err);
      if (this.errorHandle) {
        this.errorHandle(err);
      }
      return false
    }
  }

 setupAudioProcessing(stream: MediaStream): void {
    this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
    this.source = this.audioContext.createMediaStreamSource(stream);
    this.analyser = this.audioContext.createAnalyser();
    this.source.connect(this.analyser);
    this.analyser.fftSize = 2048;
    this.isVisualizing = true;  
  }

 onDataAvailable() {
    this.mediaRecorder?.addEventListener('dataavailable', (event) => {
      if (event.data.size > 0) {
        this.audioChunks.push(event.data);
        this.generateAudioBlob();
      }
    });
  }

  async start(): Promise<boolean> {
    if (!this.mediaRecorder) {
      const ok = await this.ensureStream();
      if (!ok) {
        window.$msg.error("请检查是否有麦克风")
        return false
      }
    }
    try {
      this.mediaRecorder.start();
    } catch (err) {
      if (this.errorHandle) {
        this.errorHandle(err);
      }
      return false
    }
    this.isVisualizing = true;  
    app = createApp(XwyaWave, {
      isVisualizing: this.isVisualizing,
      analyser: this.analyser,
      v:thea
    })
    this.createWave()
    return true
  }

  stop(f) {
    this.falg = f
    this.mediaRecorder?.stop();
    this.isVisualizing = false;  
    app && app.unmount()
  }

 generateAudioBlob() {
    if (this.audioChunks.length > 0) {
      if (this.falg){
        const blob = new Blob(this.audioChunks, { type: 'audio/wav' });
        const url = URL.createObjectURL(blob);
        this.audioChunks = [];
        this.falg = false;
      if (this.successHandle) {
        this.successHandle(blob, url);
      }
      }

    }
  }

  onSuccess(handle: (data: Blob, url: string) => void) {
    this.successHandle = handle;
  }

  onError(handle: (err: any) => void): void {
    this.errorHandle = handle;
  }

  createWave() {
    app.mount(el)
  }
}

export default Media;
