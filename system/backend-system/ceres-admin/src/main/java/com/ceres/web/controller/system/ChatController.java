package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Resource
    private IChatService chatService;

    @Log(title = "语音识别")
    @PostMapping("audioTrans2Text")
    public AjaxResult audioTrans2Text(@RequestParam("audio") MultipartFile audio) {
        return AjaxResult.success("语音转换文字成功", chatService.audioTrans2Text(audio));
    }

    @Log(title = "通过聊天框和模型交互")
    @PostMapping
    public AjaxResult chat (@RequestBody Map<String, String> map) {
        return AjaxResult.success(chatService.chat(map));
    }

    @Log(title = "文本转语音")
    @PostMapping(value="text2audio")
    public AjaxResult text2audio(@RequestBody Map<String, String> map) throws UnsupportedEncodingException {
        return AjaxResult.success("", chatService.text2audio(map.get("text"), map.get("langue")));
    }

    @Log(title = "点赞")
    @GetMapping("up")
    public AjaxResult up (@RequestParam("id") Long id, @RequestParam("up") Integer up) {
        return AjaxResult.success(chatService.up(id, up));
    }

    @Log(title = "清空模型后端in-memory对话上下文")
    @PostMapping("clear")
    public AjaxResult clear(@RequestBody Map<String, String> map) {
        chatService.clearChat(map);
        return AjaxResult.success();
    }
}
