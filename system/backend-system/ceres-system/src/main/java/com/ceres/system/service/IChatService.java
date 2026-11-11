package com.ceres.system.service;

import com.ceres.system.domain.vo.resp.ChatResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface IChatService {

    String audioTrans2Text(MultipartFile audio);

    List<ChatResp> chat(Map<String, String> map);

    int up(Long id, Integer up);

    public String text2audio(String text, String langue);

    /** Clear only the model backend's in-memory conversation context. Persisted chat_history rows remain intact. */
    void clearChat(Map<String, String> map);

}
