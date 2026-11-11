<template>
  <div>
    <button @click="startRecording" :disabled="isRecording">开始录音并转文字</button>
    <button @click="stopRecording" :disabled="!isRecording">停止录音</button>
    <audio v-if="audioUrl" :src="audioUrl" controls></audio>
    <p class="!text-inherit">转文字结果：</p>
    <div>{{ transcript }}</div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  setup() {
    const isRecording = ref(false);
    const mediaRecorder = ref(null);
    const audioChunks = ref([]);
    const audioUrl = ref('');
    const transcript = ref('');
    let recognition = null;

    if ('webkitSpeechRecognition' in window) {
      recognition = new window.webkitSpeechRecognition();
    } else if ('SpeechRecognition' in window) {
      recognition = new window.SpeechRecognition();
    } else {
      alert('Your browser does not support Speech Recognition');
    }

    const startRecognition = () => {
      if (!recognition) return;

      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'zh-CN';  

      recognition.onresult = (event) => {
        let interimTranscript = '';
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          if (event.results[i].isFinal) {
            transcript.value += event.results[i][0].transcript;
          } else {
            interimTranscript += event.results[i][0].transcript;
          }
        }
      };

      recognition.onerror = (event) => {
        console.error('Speech recognition error', event);
      };

      recognition.start();
    };

    const stopRecognition = () => {
      if (recognition) {
        recognition.stop();
      }
    };

    const startRecording = async () => {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          mediaRecorder.value = new MediaRecorder(stream);

          mediaRecorder.value.ondataavailable = (event) => {
            audioChunks.value.push(event.data);
          };

          mediaRecorder.value.onstop = () => {
            const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' });
            audioUrl.value = URL.createObjectURL(audioBlob);
            audioChunks.value = [];
          };

          mediaRecorder.value.start();
          isRecording.value = true;
          startRecognition();
        } catch (err) {
          console.error('Error accessing microphone:', err);
        }
      } else {
        alert('Your browser does not support audio recording');
      }
    };

    const stopRecording = () => {
      if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
        mediaRecorder.value.stop();
        isRecording.value = false;
        stopRecognition();
      }
    };

    return {
      isRecording,
      audioUrl,
      transcript,
      startRecording,
      stopRecording,
    };
  },
};
</script>

<style scoped>
button {
  margin: 5px;
}
</style>
