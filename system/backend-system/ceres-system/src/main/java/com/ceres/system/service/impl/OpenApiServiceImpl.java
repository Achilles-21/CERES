package com.ceres.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceres.common.exception.ServiceException;
import com.ceres.system.domain.*;
import com.ceres.system.domain.vo.resp.openApi.*;
import com.ceres.system.mapper.ChatHistoryMapper;
import com.ceres.system.mapper.NotesMapper;
import com.ceres.system.mapper.QueMapper;
import com.ceres.system.mapper.QueRecordMapper;
import com.ceres.system.service.ICaseLibraryService;
import com.ceres.system.service.ICasePicsService;
import com.ceres.system.service.IOpenApiService;
import com.ceres.system.util.AnswerNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OpenApiServiceImpl implements IOpenApiService {

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;
    @Autowired
    private QueMapper queMapper;
    @Autowired
    private QueRecordMapper queRecordMapper;
    @Autowired
    private NotesMapper notesMapper;
    @Autowired
    private ICaseLibraryService caseLibraryService;
    @Autowired
    private ICasePicsService casePicsService;

    @Override
    public List<ChatHistoryResp> getChatHistory(Long user_id) {
        LambdaQueryWrapper<ChatHistory> eq = new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, user_id)
                .orderByDesc(ChatHistory::getTimeSpan);
        List<ChatHistory> chatHistories = chatHistoryMapper.selectList(eq);
        List<ConversationsResp> conversationsRespList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ChatHistory chatHistory : chatHistories) {
            ConversationsResp conversationsResp = new ConversationsResp();
            conversationsResp.setTime_span(chatHistory.getTimeSpan().format(fmt));
            conversationsResp.setRole(chatHistory.getRole());
            conversationsResp.setText(chatHistory.getText());
            conversationsRespList.add(conversationsResp);
        }
        ChatHistoryResp chatHistoryResp = new ChatHistoryResp();
        chatHistoryResp.setUser_id(user_id);
        chatHistoryResp.setConversations(conversationsRespList);
        List<ChatHistoryResp> chatHistoryRespList = ListUtil.of(chatHistoryResp);
        return chatHistoryRespList;
    }

    @Override
    public List<CaseReportResp> getExampleAnalysis(String concept) {
        if(StrUtil.isBlank(concept)){
            return ListUtil.empty();
        }
        LambdaQueryWrapper<CaseLibrary> wrapper = new LambdaQueryWrapper<CaseLibrary>().like(CaseLibrary::getName, concept);
        List<CaseLibrary> caseLibraryList = caseLibraryService.list(wrapper);
        Random random = new Random();
        CaseLibrary caseLibrary = caseLibraryList.get(random.nextInt(caseLibraryList.size()));
        CaseReportResp caseReportResp = new CaseReportResp();
        caseReportResp.setIdx(caseLibrary.getId());
        if(StrUtil.isNotBlank(caseLibrary.getName())){
            caseReportResp.setLabel(caseLibrary.getName());
        }
        if(StrUtil.isNotBlank(caseLibrary.getPatient())){
            caseReportResp.setContext(caseLibrary.getPatient());
        }
        if(caseLibrary.getQuestionsNumber()!=null){
            caseReportResp.setN_qa(caseLibrary.getQuestionsNumber());
            List<QAResp> qaResps = new ArrayList<>();
            if(StrUtil.isNotBlank(caseLibrary.getQuestion1())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion1());
                qaResp.setAnswer(caseLibrary.getAnswer1());
                qaResps.add(qaResp);
            }
            if(StrUtil.isNotBlank(caseLibrary.getQuestion2())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion2());
                qaResp.setAnswer(caseLibrary.getAnswer2());
                qaResps.add(qaResp);
            }
            if(StrUtil.isNotBlank(caseLibrary.getQuestion3())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion3());
                qaResp.setAnswer(caseLibrary.getAnswer3());
                qaResps.add(qaResp);
            }
            if(StrUtil.isNotBlank(caseLibrary.getQuestion4())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion4());
                qaResp.setAnswer(caseLibrary.getAnswer4());
                qaResps.add(qaResp);
            }
            if(StrUtil.isNotBlank(caseLibrary.getQuestion5())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion5());
                qaResp.setAnswer(caseLibrary.getAnswer5());
                qaResps.add(qaResp);
            }
            if(StrUtil.isNotBlank(caseLibrary.getQuestion6())){
                QAResp qaResp = new QAResp();
                qaResp.setQuestion(caseLibrary.getQuestion6());
                qaResp.setAnswer(caseLibrary.getAnswer6());
                qaResps.add(qaResp);
            }
            caseReportResp.setQa(qaResps);
        }
        return ListUtil.of(caseReportResp);
    }

    @Override
    public List<QueResponse> getExercise(Long user_id, Long course_id) {
        LambdaQueryWrapper<Que> eq = new LambdaQueryWrapper<Que>().eq(course_id!=null, Que::getCourseId, course_id).eq(Que::getDelFlag, 0);
        List<Que> queList = queMapper.selectList(eq);
        LambdaQueryWrapper<QueRecord> lambdaQueryWrapper = new LambdaQueryWrapper<QueRecord>().eq(QueRecord::getUserId, user_id);
        List<QueRecord> queRecordList = queRecordMapper.selectList(lambdaQueryWrapper);
        Map<Long, Que> queMap = queList.stream().collect(Collectors.toMap(Que::getId, q -> q));
        List<QueResponse> queResponseList = new ArrayList<>();
        for (QueRecord queRecord : queRecordList) {
            Que que = queMap.get(queRecord.getQueId());
            if(ObjectUtil.isNotEmpty(que)){
                QueResponse queResponse = new QueResponse();
                queResponse.setQuestion_id(que.getId());
                queResponse.setQuestion(que.getDescription());
                List<String> choiceList = new ArrayList<>();
                choiceList.add(que.getChoiceA());
                choiceList.add(que.getChoiceB());
                choiceList.add(que.getChoiceC());
                choiceList.add(que.getChoiceD());
                choiceList.add(que.getChoiceE());
                choiceList.add(que.getChoiceF());
                queResponse.setOptions(choiceList);
                String answer = que.getAnswer();
                String answerNumStr = AnswerNumberUtil.answerToNumber(answer);
                queResponse.setAnswer(Integer.valueOf(answerNumStr));
                queResponse.setSolution(que.getAnswerExplain());
                queResponse.setImage_urls(ListUtil.of(que.getImgUrl()));
                String recordAnswer = queRecord.getAnswer();
                String answerToNumber = AnswerNumberUtil.answerToNumber(recordAnswer);
                queResponse.setUser_ans(Long.valueOf(answerToNumber));
                queResponseList.add(queResponse);
            }
        }
        return queResponseList;
    }

    @Override
    public List<NotesResponse> getNotes(Long user_id, Long course_id) {
        LambdaQueryWrapper<Notes> eq = new LambdaQueryWrapper<Notes>().eq(Notes::getUserId, user_id).eq(course_id!=null, Notes::getVideoId, course_id).eq(Notes::getDelFlag, 0);
        List<Notes> notesList = notesMapper.selectList(eq);
        List<NotesResponse> notesResponseList = new ArrayList<>();
        for (Notes notes : notesList) {
            NotesResponse notesResponse = new NotesResponse();
            notesResponse.setUser_id(notes.getUserId());
            notesResponse.setCourse_id(notes.getVideoId());
            if(StrUtil.isNotBlank(notes.getContent())){
                String content = notes.getContent();
                String png = ".png";
                String jpg = ".jpg";
                if(content.contains(jpg) || content.contains(png)){
                    notesResponse.setImage_urls(List.of(content));
                }else {
                    notesResponse.setNote_text(notes.getContent());
                }
            }
            notesResponseList.add(notesResponse);
        }
        return notesResponseList;
    }
}
