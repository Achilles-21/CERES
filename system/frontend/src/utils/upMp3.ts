
  const upMp3 = async (formData: FormData):Promise => {
    const response = await fetch(import.meta.env.VITE_APP_URL+"/chat/audioTrans2Text", {
        method: 'POST',
        body: formData,
    });
    return  response.json();

}


export default upMp3;
