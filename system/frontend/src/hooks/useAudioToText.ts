const useAudioToText = () => {
  // TODO: Implement audio to text conversion
  const isRecognizing = ref(false);
  const transcript = ref('');
  let recognition = null;
  const initRecognition = () => {
    if ('webkitSpeechRecognition' in window) {
      recognition = new window.webkitSpeechRecognition();
    } else if ('SpeechRecognition' in window) {
      recognition = new window.SpeechRecognition();
    } else {
      alert('Your browser does not support Speech Recognition');
    }
  }
  const startRecognition = () => {
    if (!recognition) return;
    console.log("开始语音识别");

    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'zh-Hans-CN';  

    recognition.onstart = () => {
      isRecognizing.value = true;
    };

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

    recognition.onend = () => {
      isRecognizing.value = false;
    };
    recognition.start();
  }
  const stopRecognition = () => {
    console.log("结束语音识别");

    if (recognition && recognition.state !== 'inactive') {
      recognition.stop();
    }
  };
  onMounted(() => {
    initRecognition()
  })
  return {
    startRecognition,
    stopRecognition,
    transcript,
    isRecognizing
  }

}
export default useAudioToText;
