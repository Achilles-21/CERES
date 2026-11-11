package com.ceres.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.MinioUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.constant.ModelConstant;
import com.ceres.system.domain.ChatHistory;
import com.ceres.system.domain.FileInfo;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.common.utils.StringUtils;
import com.ceres.system.domain.VideoPop;
import com.ceres.system.domain.vo.resp.ChatResp;
import com.ceres.system.domain.vo.resp.RecommendQueResp;
import com.ceres.system.enums.ChatRoleEnum;
import com.ceres.system.enums.ModeCodeEnum;
import com.ceres.system.mapper.ChatHistoryMapper;
import com.ceres.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {

    @Resource
    private ChatHistoryMapper chatHistoryMapper;
    @Resource
    private IQueService queService;
    @Resource
    private IVideoPopQueService videoPopQueService;
    @Resource
    private IResourceVideoService resourceVideoService;
    @Resource
    private IFileService fileService;
    @Autowired
    private MinioUtils minioUtils;

    @Override
    public String audioTrans2Text(MultipartFile audio) {
        if (audio.isEmpty()) {
            return "";
        }
        File tempFile;
        File tmpFile;
        try {
            String originalFilename = audio.getOriginalFilename();
            tempFile = File.createTempFile("temp", originalFilename);
            tmpFile = File.createTempFile("tmp", originalFilename);
            audio.transferTo(tempFile);

            AudioAttributes theAudio = new AudioAttributes();
            theAudio.setSamplingRate(16000);
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setAudioAttributes(theAudio);
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(tempFile), tmpFile, attrs);
        } catch (Exception e) {
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("file", tmpFile);
        String post;

        try {
            post = HttpUtil.post(ModelConstant.AUDIO_TO_STRING_URL, paramMap);
        } finally {
            boolean temp = tempFile.delete();
            boolean tmp = tmpFile.delete();
            log.info("清理临时文件temp：{}", temp);
            log.info("清理临时文件tmp：{}", tmp);
        }
        if("Internal Server Error".equals(post) || StrUtil.isBlank(post) || "null".equals(post)){
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }

        JSONObject jsonObject = JSONObject.parseObject(post);
        if (0 != jsonObject.getInteger(ModelConstant.ERROR_CODE)) {
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }
        return jsonObject.getString(ModelConstant.OUTPUT_TEXT);
    }

    @Transactional
    @Override
    public List<ChatResp> chat(Map<String, String> map) {
        int isQue = Integer.parseInt(map.get("isQue"));
        if (StrUtil.isEmpty(map.get("query")) && StrUtil.isEmpty(map.get("image")) && Integer.parseInt(map.get("isQue")) == 0 && Integer.parseInt(map.get("modelType")) == 0) {
            try {
                Map<String, Object> clearParam = new HashMap<>();
                clearParam.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
                clearParam.put("conv_type", 0);
                HttpUtil.post(ModelConstant.MODEL_CLEAR_URL, JSONObject.toJSONString(clearParam));
            } catch (Exception e) {
                log.warn("Failed to clear model chat history for user {}: {}",
                        SecurityUtils.getUserId(), e.getMessage());
            }

            List<ChatResp> chatResponse = new ArrayList<>();
            ChatResp aiChatResp = new ChatResp();
            aiChatResp.setRole(ChatRoleEnum.SYSTEM.value());
            aiChatResp.setContent(MessageUtils.message("model.hello"));
            aiChatResp.setPicture(1);
            aiChatResp.setUp(-1);
            chatResponse.add(aiChatResp);
            return chatResponse;
        }

        if(StrUtil.isEmpty(map.get("query")) && StrUtil.isEmpty(map.get("image")) && Integer.parseInt(map.get("modelType")) == 2){
            List<ChatResp> chatRespList = new ArrayList<>();
            ChatResp aiChatResp = new ChatResp();
            aiChatResp.setRole(ChatRoleEnum.SYSTEM.value());
            aiChatResp.setPicture(1);
            aiChatResp.setContent(MessageUtils.message("model.question.hello"));
            chatRespList.add(aiChatResp);
            return chatRespList;
        }

        if (isQue == 1) {
            Map<String, String> mapResp = queService.send(map);
            List<ChatResp> chatRespList = new ArrayList<>();
            ChatResp userChatResp = new ChatResp();
            userChatResp.setRole(ChatRoleEnum.USER.value());
            userChatResp.setContent(map.get("userAnswer"));
            userChatResp.setPicture(0);
            chatRespList.add(userChatResp);
            LambdaQueryWrapper<VideoPop> lambdaQueryWrapper = new LambdaQueryWrapper<VideoPop>().eq(VideoPop::getId, map.get("popId")) ;
            List<VideoPop> videoPopList = videoPopQueService.list(lambdaQueryWrapper);
            FileInfo fileInfo = null;
            if(CollUtil.isNotEmpty(videoPopList)){
                VideoPop videoPop = videoPopList.get(0);
                if(videoPop.getPopVideoId()!=null){
                    ResourceVideo resourceVideo = resourceVideoService.getById(videoPop.getPopVideoId());
                    fileInfo = fileService.getById(resourceVideo.getFileId());
                }
            }
            chatRespList.add(new ChatResp(ChatRoleEnum.SYSTEM.value(), mapResp.get("detail"), 1, 0, null, mapResp.get("res"), ObjUtil.isNotEmpty(fileInfo)? Objects.requireNonNull(fileInfo).getFileUrl():null, null, null, null));

            for(ChatResp chatResp : chatRespList) {
                if(StringUtils.isNotEmpty(chatResp.getContent())){
                    ChatHistory userChatHistory = new ChatHistory();
                    userChatHistory.setUserId(SecurityUtils.getUserId());
                    userChatHistory.setRole(chatResp.getRole());
                    userChatHistory.setTimeSpan(LocalDateTime.now());
                    userChatHistory.setText(chatResp.getContent());
                    chatHistoryMapper.insert(userChatHistory);
                }
            }

            return chatRespList;
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
        paramMap.put(ModelConstant.TIME_SPAN, timeSpan);

        String inputText = "";
        String content = map.get("query");
        String br = "<br>";
        String div = "<div";
        if (content.contains(br)) {
            String[] strings = content.split(div);
            String query = strings[0];
            if(StrUtil.isNotBlank(query)) {
                inputText = query;
            }
        } else {
            String divClose = "</div>";
            if (content.contains(divClose)) {
                String[] split = content.split(div);
                String prefixQuery = split[0];
                if (StrUtil.isBlank(prefixQuery)) {
                    String[] strings = content.split(divClose);
                    inputText = strings[1];
                } else {
                    String[] strings = content.split(divClose);
                    String query = strings[1];
                    inputText = prefixQuery + query;
                }
            } else {
                inputText = content;
            }
        }
        paramMap.put(ModelConstant.INPUT_TEXT, inputText);

        int modelType = Integer.parseInt(map.get("modelType"));
        Integer modeCode = null;
        if(modelType==0){
            modeCode = ModeCodeEnum.QUESTION.getCode();
        }else if(modelType==2){
            modeCode = ModeCodeEnum.TEST_REQUEST.getCode();
        }
        paramMap.put(ModelConstant.MODE_CODE, modeCode);
        String image = "";
        if(modelType==0){
            if (StrUtil.isNotBlank(map.get("image"))) {
                image = map.get("image");
            }
        }
        paramMap.put(ModelConstant.Image, image);
        String result = HttpUtil.post(ModelConstant.MODEL_URL, JSONObject.toJSONString(paramMap));
        if("Internal Server Error".equals(result) || StrUtil.isBlank(result) || "null".equals(result)){
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        List<ChatResp> chatRespList = new ArrayList<>();
        String outputText = jsonObject.getString(ModelConstant.OUTPUT_TEXT);
        String query = map.get("query");
        ChatResp userChatResp = new ChatResp();
        userChatResp.setRole(ChatRoleEnum.USER.value());
        userChatResp.setContent(query);
        userChatResp.setPicture(0);
        chatRespList.add(userChatResp);
        ChatResp aiChatResp = new ChatResp();
        aiChatResp.setRole(ChatRoleEnum.SYSTEM.value());
        aiChatResp.setPicture(1);

        if(modelType==2){
            aiChatResp.setContent(MessageUtils.message("model.question.success"));
            List<String> questionStrIds = jsonObject.getList(ModelConstant.QUESTION_IDS, String.class);
            ArrayList<Long> listIds = questionStrIds.stream().map(Long::parseLong).distinct().collect(Collectors.toCollection(ArrayList::new));
            if(CollUtil.isNotEmpty(listIds)){
                List<RecommendQueResp> recommendQueRespList = queService.getQueByIds(listIds);
                Map<Long, Long> courseIdCountMap = recommendQueRespList.stream().collect(Collectors.groupingBy(RecommendQueResp::getCourseId, Collectors.counting()));
                log.info("根据题目统计的课程信息(课程id,出现次数):{}", courseIdCountMap);
                long maxCount = courseIdCountMap.values().stream().mapToLong(l -> l).max().orElse(0);
                if(maxCount!=0){
                    List<Long> maxCountCourseIds = courseIdCountMap.entrySet().stream()
                            .filter(entry -> entry.getValue() == maxCount)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    if (maxCountCourseIds.size() > 1) {
                        int randomIndex = ThreadLocalRandom.current().nextInt(maxCountCourseIds.size());
                        aiChatResp.setCourseId(maxCountCourseIds.get(randomIndex));
                    } else {
                        aiChatResp.setCourseId(maxCountCourseIds.get(0));
                    }
                }
                aiChatResp.setRecommendQueRespList(recommendQueRespList);
            }
            chatRespList.add(aiChatResp);
            return chatRespList;
        }else if(modelType==0){
            JSONObject topicsObj = jsonObject.getJSONObject("topics");
            if (topicsObj != null) {
                aiChatResp.setTopics(topicsObj.toJavaObject(Map.class));
            }
            if (StrUtil.isNotBlank(jsonObject.getString("image"))) {
                String base64ImageStr = jsonObject.getString("image");
                byte[] images = Base64.getDecoder().decode(base64ImageStr);
                String base64ImageUrl = minioUtils.uploadBase64Image(new ByteArrayInputStream(images));
                String modelReply = ModelConstant.SHOW_IMAGE_1 + base64ImageUrl + ModelConstant.SHOW_IMAGE_2 + base64ImageUrl + ModelConstant.SHOW_IMAGE_3 + outputText;
                aiChatResp.setContent(modelReply);
                chatRespList.add(aiChatResp);
            } else {
                aiChatResp.setContent(outputText);
                chatRespList.add(aiChatResp);
            }
        }

        ChatHistory userChatHistory = new ChatHistory();
        userChatHistory.setUserId(SecurityUtils.getUserId());
        userChatHistory.setRole(ChatRoleEnum.USER.value());
        userChatHistory.setTimeSpan(LocalDateTime.now());
        userChatHistory.setText(query);
        chatHistoryMapper.insert(userChatHistory);

        ChatHistory aiChatHistory = new ChatHistory();
        aiChatHistory.setUserId(SecurityUtils.getUserId());
        aiChatHistory.setRole(ChatRoleEnum.SYSTEM.value());
        aiChatHistory.setText(aiChatResp.getContent());
        aiChatHistory.setTimeSpan(LocalDateTime.now());
        chatHistoryMapper.insert(aiChatHistory);

        return chatRespList;
    }

    @Override
    public String text2audio(String text, String langue) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ModelConstant.AUDIO_TEXT, text);
        paramMap.put(ModelConstant.AUDIO_GENDER, 1);
        paramMap.put(ModelConstant.AUDIO_LANGUE, langue);

        byte[] audioBytes = HttpUtil.createPost(ModelConstant.TEXT_TO_AUDIO_URL)
            .body(JSONObject.toJSONString(paramMap))
            .execute()
            .bodyBytes();

        if (audioBytes == null || audioBytes.length == 0) {
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }

        return Base64.getEncoder().encodeToString(audioBytes);
    }

    @Override
    public int up(Long id, Integer up) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(id);
        chatHistory.setUp(up);
        return chatHistoryMapper.updateById(chatHistory);
    }

    @Override
    public void clearChat(Map<String, String> map) {
        Long userId = SecurityUtils.getUserId();
        int convType = 0;
        if (map != null && StrUtil.isNotBlank(map.get("modelType"))) {
            convType = Integer.parseInt(map.get("modelType"));
        }

        Map<String, Object> body = new HashMap<>();
        body.put(ModelConstant.USER_ID, userId.toString());
        body.put("conv_type", convType);
        String resp = HttpUtil.post(ModelConstant.MODEL_CLEAR_URL, JSONObject.toJSONString(body));
        log.info("Cleared model chat ctx userId={} conv_type={} resp={}", userId, convType, resp);
    }

}
