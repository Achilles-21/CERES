package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.constant.ModelConstant;
import com.ceres.system.domain.CaseLibrary;
import com.ceres.system.domain.CasePics;
import com.ceres.system.domain.CaseQueBack;
import com.ceres.system.domain.vo.req.CaseReq;
import com.ceres.system.domain.vo.req.EvaluateReq;
import com.ceres.system.domain.vo.resp.CaseAnalysisResp;
import com.ceres.system.domain.vo.resp.CaseResp;
import com.ceres.system.enums.ModeCodeEnum;
import com.ceres.system.service.ICaseLibraryService;
import com.ceres.system.service.ICasePicsService;
import com.ceres.system.service.ICaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CaseServiceImpl implements ICaseService {

    @Resource
    private ICaseLibraryService caseLibraryService;
    @Resource
    private ICasePicsService casePicsService;

    private int showCount=0;

    @Override
    @Transactional
    public void addCase(CaseReq caseReq) {
        CaseLibrary caseLibrary = BeanUtil.toBean(caseReq, CaseLibrary.class);
        caseLibraryService.save(caseLibrary);
        if (CollectionUtil.isEmpty(caseReq.getCasePicsList())) {
            return;
        }
        if (CollUtil.isNotEmpty(caseReq.getCasePicsList())) {
            List<String> picsList = caseReq.getCasePicsList();
            List<CasePics> casePicsList = new ArrayList<>();
            for (String pic : picsList) {
                CasePics casePics = new CasePics();
                casePics.setCaseId(caseLibrary.getId());
                casePics.setPic(pic);
                casePicsList.add(casePics);
            }
            casePicsService.saveBatch(casePicsList);
        }
    }

    @Override
    @Transactional
    public void updateCase(CaseReq caseReq) {
        CaseLibrary caseLibrary = BeanUtil.toBean(caseReq, CaseLibrary.class);
        caseLibraryService.updateById(caseLibrary);
        // Replace the previous case-image associations before adding the new ones.
        if (CollectionUtil.isEmpty(caseReq.getCasePicsList())) {
            return;
        }
        LambdaQueryWrapper<CasePics> eq = new LambdaQueryWrapper<CasePics>().eq(CasePics::getCaseId, caseReq.getId());
        casePicsService.remove(eq);
        List<String> picsList = caseReq.getCasePicsList();
        List<CasePics> casePicsList = new ArrayList<>();
        for (String pic : picsList) {
            CasePics casePics = new CasePics();
            casePics.setCaseId(caseLibrary.getId());
            casePics.setPic(pic);
            casePicsList.add(casePics);
        }
        casePicsService.saveBatch(casePicsList);
    }

    @Override
    @Transactional
    public void deleteCase(Long id) {
        caseLibraryService.removeById(id);
        LambdaQueryWrapper<CasePics> eq = new LambdaQueryWrapper<CasePics>().eq(CasePics::getCaseId, id);
        casePicsService.remove(eq);
    }

    @Override
    public CaseResp getById(Long id) {
        CaseLibrary caseLibrary = caseLibraryService.getById(id);
        LambdaQueryWrapper<CasePics> eq = new LambdaQueryWrapper<CasePics>().eq(CasePics::getCaseId, id);
        List<CasePics> casePicsList = casePicsService.list(eq);
        CaseResp caseResp = BeanUtil.toBean(caseLibrary, CaseResp.class);
        caseResp.setCasePicsList(casePicsList);
        return caseResp;
    }

    @Override
    public PageResult pageQuery(Integer pageNum, Integer pageSize, String patient) {
        LambdaQueryWrapper<CaseLibrary> eq = new LambdaQueryWrapper<CaseLibrary>().like(StrUtil.isNotBlank(patient), CaseLibrary::getPatient, patient);
        Page<CaseLibrary> page = new Page<>(pageNum, pageSize);
        Page<CaseLibrary> caseLibraryPage = caseLibraryService.page(page, eq);
        if (CollectionUtil.isEmpty(caseLibraryPage.getRecords())) {
            return new PageResult(ListUtil.empty(), 0, pageNum, pageSize);
        }
        List<CaseLibrary> caseLibraryList = caseLibraryPage.getRecords();
        List<CaseResp> caseRespList = BeanUtil.copyToList(caseLibraryList, CaseResp.class);
        List<Long> caseLibraryIds = caseLibraryList.stream().map(CaseLibrary::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CasePics> lambdaQueryWrapper = new LambdaQueryWrapper<CasePics>().in(CasePics::getCaseId, caseLibraryIds);
        List<CasePics> casePicsList = casePicsService.list(lambdaQueryWrapper);
        for (CaseResp caseResp : caseRespList) {
            List<CasePics> casePicsArrayList = new ArrayList<>();
            for (CasePics casePics : casePicsList) {
                if (caseResp.getId().equals(casePics.getCaseId())) {
                    casePicsArrayList.add(casePics);
                }
            }
            caseResp.setCasePicsList(casePicsArrayList);
        }
        return new PageResult(caseRespList, caseLibraryPage.getTotal(), Long.valueOf(caseLibraryPage.getCurrent()).intValue(), Long.valueOf(caseLibraryPage.getSize()).intValue());
    }

    @Override
    public List<CaseAnalysisResp> analysis(Map<String, String> map) {
        if (StrUtil.isBlank(map.get("query"))) {
            CaseAnalysisResp caseAnalysisResp = new CaseAnalysisResp();
            caseAnalysisResp.setContent(MessageUtils.message("model.case.hello"));
            caseAnalysisResp.setRole("system");
            caseAnalysisResp.setPicture(1);
            return List.of(caseAnalysisResp);
        }
        Map<String, String> paramMap = new HashMap<>();
        Long userId = SecurityUtils.getUserId();
        paramMap.put(ModelConstant.USER_ID, userId.toString());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
        paramMap.put(ModelConstant.TIME_SPAN, timeSpan);
        String content = map.get("query");
        Integer code = null;
        String inputText = "";
        CaseLibrary caseLibrary = null;
        String br = "<br>";
        String div = "<div";
        if (content.contains(br)) {
            String[] strings = content.split(div);
            String query = strings[0];
            if (StrUtil.isNotBlank(query)) {
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
        if (StrUtil.isBlank(map.get("reportId")) || 0 == Long.parseLong(map.get("reportId"))) {
            // Reset image-display state for each case to avoid accumulation.
            showCount=0;
            code = ModeCodeEnum.CASE_REPORT_REQUEST.getCode();
        } else {
            showCount++;
            caseLibrary = caseLibraryService.getById(map.get("reportId"));
            inputText = "<c>" + caseLibrary.getPatient() + "</c>" + "<s>" + inputText + "</s>";
            code = ModeCodeEnum.CASE_REPORT_PATIENT.getCode();
        }
        paramMap.put(ModelConstant.INPUT_TEXT, inputText);
        paramMap.put(ModelConstant.MODE_CODE, code.toString());
        String image = "";
        paramMap.put(ModelConstant.Image, image);
        String result = HttpUtil.post(ModelConstant.MODEL_URL, JSONObject.toJSONString(paramMap));
        if ("Internal Server Error".equals(result) || StrUtil.isBlank(result) || "null".equals(result)) {
            throw new RuntimeException(MessageUtils.message("model.server.error"));
        }

        JSONObject jsonObject = JSONObject.parseObject(result);
        String patientText = jsonObject.getString(ModelConstant.PATIENT_TEXT);
        if(StrUtil.isBlank(map.get("reportId")) || 0 != Long.parseLong(map.get("reportId")) || (jsonObject.getLong(ModelConstant.REPORT_ID)!=null && jsonObject.getLong(ModelConstant.REPORT_ID)!=0)){
            Long reportId=null;
            if(jsonObject.getLong(ModelConstant.REPORT_ID)!=null && jsonObject.getLong(ModelConstant.REPORT_ID)!=0){
                reportId = jsonObject.getLong(ModelConstant.REPORT_ID);
            }else{
                reportId=Long.parseLong(map.get("reportId"));
            }
            caseLibrary = caseLibraryService.getById(reportId);
        }
        List<CaseAnalysisResp> caseAnalysisRespList = new ArrayList<>();
        CaseAnalysisResp userAnalysisResp = new CaseAnalysisResp();
        userAnalysisResp.setRole("user");
        userAnalysisResp.setContent(content);
        userAnalysisResp.setPicture(0);
        CaseAnalysisResp aiAnalysisResp = new CaseAnalysisResp();
        if (ObjUtil.isNotEmpty(caseLibrary)) {
            List<CaseQueBack> caseQueBackList = new ArrayList<>();
            if (StrUtil.isNotBlank(caseLibrary.getQuestion1())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion1());
                caseQueBack.setAnswer(caseLibrary.getAnswer1());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            if (StrUtil.isNotBlank(caseLibrary.getQuestion2())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion2());
                caseQueBack.setAnswer(caseLibrary.getAnswer2());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            if (StrUtil.isNotBlank(caseLibrary.getQuestion3())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion3());
                caseQueBack.setAnswer(caseLibrary.getAnswer3());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            if (StrUtil.isNotBlank(caseLibrary.getQuestion4())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion4());
                caseQueBack.setAnswer(caseLibrary.getAnswer4());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            if (StrUtil.isNotBlank(caseLibrary.getQuestion5())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion5());
                caseQueBack.setAnswer(caseLibrary.getAnswer5());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            if (StrUtil.isNotBlank(caseLibrary.getQuestion6())) {
                CaseQueBack caseQueBack = new CaseQueBack();
                caseQueBack.setQuestion(caseLibrary.getQuestion6());
                caseQueBack.setAnswer(caseLibrary.getAnswer6());
                caseQueBack.setPatient(caseLibrary.getPatient());
                caseQueBackList.add(caseQueBack);
            }
            aiAnalysisResp.setCaseQueBackList(caseQueBackList);
            aiAnalysisResp.setQuestionsNumber(caseLibrary.getQuestionsNumber());
            aiAnalysisResp.setPatient(caseLibrary.getPatient());
            aiAnalysisResp.setPicture(caseLibrary.getPatientImage());
            aiAnalysisResp.setRole("patient");
        }
        if(Long.parseLong(map.get("reportId"))==0 && jsonObject.getLong(ModelConstant.REPORT_ID)==0){
            aiAnalysisResp.setPicture(1);
            aiAnalysisResp.setRole("system");
        }
        aiAnalysisResp.setReportId(jsonObject.getLong(ModelConstant.REPORT_ID));
        LambdaQueryWrapper<CasePics> eq = new LambdaQueryWrapper<CasePics>().eq(CasePics::getCaseId, Long.parseLong(map.get("reportId")));
        List<CasePics> casePicsList = casePicsService.list(eq);
        if (CollUtil.isNotEmpty(casePicsList) && showCount == 1) {
            List<String> imageUrls = casePicsList.stream().map(CasePics::getPic).collect(Collectors.toList());
            aiAnalysisResp.setImageUrls(imageUrls);
        }
        aiAnalysisResp.setContent(patientText);
        caseAnalysisRespList.add(userAnalysisResp);
        caseAnalysisRespList.add(aiAnalysisResp);
        return caseAnalysisRespList;
    }

    @Override
    public CaseAnalysisResp getQue(CaseQueBack caseQueBack) {
        String que = "<q>" + caseQueBack.getQuestion() + "</q>";
        String patient = "<c>" + caseQueBack.getPatient() + "</c>";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
        paramMap.put(ModelConstant.TIME_SPAN, timeSpan);
        paramMap.put(ModelConstant.MODE_CODE, ModeCodeEnum.CASE_REPORT_DOCTOR.getCode().toString());
        paramMap.put(ModelConstant.INPUT_TEXT, que + patient);
        paramMap.put(ModelConstant.Image, "");
        String url = ModelConstant.MODEL_URL;
        String result = HttpUtil.post(url, JSONObject.toJSONString(paramMap));
        if ("Internal Server Error".equals(result) || StrUtil.isBlank(result) || "null".equals(result)) {
            throw new ServiceException(MessageUtils.message("model.server.error"));
        }
        CaseAnalysisResp caseAnalysisResp = new CaseAnalysisResp();
        caseAnalysisResp.setRole("system");
        JSONObject jsonObject = JSONObject.parseObject(result);
        caseAnalysisResp.setContent(jsonObject.getString(ModelConstant.DOCTOR_TEXT));
        caseAnalysisResp.setPicture(1);
        return caseAnalysisResp;
    }

    @Override
    public List<CaseAnalysisResp> evaluate(EvaluateReq evaluateReq) {
        String que = "<q>" + evaluateReq.getQuestion() + "</q>";
        String answer = "<d>" + evaluateReq.getAnswer() + "</d>";
        String patient = "<c>" + evaluateReq.getPatient() + "</c>";
        String stuAnswers = "<s>" + evaluateReq.getStudentAnswers() + "</s>";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ModelConstant.USER_ID, SecurityUtils.getUserId().toString());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeSpan = LocalDateTime.now().format(dateTimeFormatter);
        paramMap.put(ModelConstant.TIME_SPAN, timeSpan);
        paramMap.put(ModelConstant.MODE_CODE, ModeCodeEnum.CASE_REPORT_DOCTOR.getCode().toString());
        paramMap.put(ModelConstant.INPUT_TEXT, que + answer + patient + stuAnswers);
        paramMap.put(ModelConstant.Image, "");
        String url = ModelConstant.MODEL_URL;
        String result = HttpUtil.post(url, JSONObject.toJSONString(paramMap));
        if ("Internal Server Error".equals(result) || StrUtil.isBlank(result) || "null".equals(result)) {
            throw new ServiceException("模型服务异常");
        }
        List<CaseAnalysisResp> caseAnalysisRespList = new ArrayList<>();
        CaseAnalysisResp userCaseAnalysisResp = new CaseAnalysisResp();
        userCaseAnalysisResp.setRole("user");
        userCaseAnalysisResp.setContent(evaluateReq.getStudentAnswers());
        userCaseAnalysisResp.setPicture(0);
        caseAnalysisRespList.add(userCaseAnalysisResp);
        CaseAnalysisResp modelCaseAnalysisResp = new CaseAnalysisResp();
        modelCaseAnalysisResp.setRole("system");
        JSONObject jsonObject = JSONObject.parseObject(result);
        modelCaseAnalysisResp.setContent(jsonObject.getString(ModelConstant.DOCTOR_TEXT));
        modelCaseAnalysisResp.setPicture(1);
        caseAnalysisRespList.add(modelCaseAnalysisResp);
        return caseAnalysisRespList;
    }

    @Override
    public CaseAnalysisResp showAnswers(Long id, String question) {
        CaseLibrary caseLibrary = caseLibraryService.getById(id);
        String answer = null;
        if (StrUtil.isNotBlank(caseLibrary.getQuestion1()) && question.equals(caseLibrary.getQuestion1())) {
            answer = caseLibrary.getAnswer1();
        } else if (StrUtil.isNotBlank(caseLibrary.getQuestion2()) && question.equals(caseLibrary.getQuestion2())) {
            answer = caseLibrary.getAnswer2();
        } else if (StrUtil.isNotBlank(caseLibrary.getQuestion3()) && question.equals(caseLibrary.getQuestion3())) {
            answer = caseLibrary.getAnswer3();
        } else if (StrUtil.isNotBlank(caseLibrary.getQuestion4()) && question.equals(caseLibrary.getQuestion4())) {
            answer = caseLibrary.getAnswer4();
        } else if (StrUtil.isNotBlank(caseLibrary.getQuestion5()) && question.equals(caseLibrary.getQuestion5())) {
            answer = caseLibrary.getAnswer5();
        } else if (StrUtil.isNotBlank(caseLibrary.getQuestion6()) && question.equals(caseLibrary.getQuestion6())) {
            answer = caseLibrary.getAnswer6();
        }
        CaseAnalysisResp caseAnalysisResp = new CaseAnalysisResp();
        caseAnalysisResp.setRole("system");
        caseAnalysisResp.setPicture(1);
        caseAnalysisResp.setContent(answer);
        return caseAnalysisResp;
    }
}
