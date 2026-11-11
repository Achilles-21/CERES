package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.CaseQueBack;
import com.ceres.system.domain.vo.req.CaseReq;
import com.ceres.system.domain.vo.req.EvaluateReq;
import com.ceres.system.domain.vo.resp.CaseAnalysisResp;
import com.ceres.system.domain.vo.resp.CaseResp;

import java.util.List;
import java.util.Map;

public interface ICaseService {

    void addCase(CaseReq caseReq);

    void updateCase(CaseReq caseReq);

    void deleteCase(Long id);

    CaseResp getById(Long id);

    PageResult pageQuery(Integer pageNum, Integer pageSize, String patient);

    List<CaseAnalysisResp> analysis(Map<String, String> map);

    CaseAnalysisResp getQue(CaseQueBack caseQueBack);

    List<CaseAnalysisResp> evaluate(EvaluateReq evaluateReq);

    CaseAnalysisResp showAnswers(Long id, String question);
}
