package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.ExamCate;
import com.ceres.system.domain.vo.req.ExamRecordListReq;
import com.ceres.system.domain.vo.req.ExamReq;
import com.ceres.system.domain.vo.req.ExamCateReq;
import com.ceres.system.domain.vo.req.PageReq;
import com.ceres.system.domain.vo.resp.ExamCateResp;
import com.ceres.system.domain.vo.resp.ExamResp;
import com.ceres.system.domain.vo.resp.RecommendExamResp;
import com.ceres.system.domain.vo.resp.RecommendExamRespPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IExamService {

    void addExam(ExamReq req);

    void deleteExam(Long id);

    void updateExam(ExamReq req);

    void updateExamStatus(Long id, String status);

    PageResult getExamList(Integer pageNum, Integer pageSize, String type, String content, String cateId);

    ExamResp getExamById(Long id);

    RecommendExamRespPage getRecommendExamList(PageReq pageReq, String cateId);

    void saveExamRecordList(ExamRecordListReq req);

    List<Map<String, String>> listAll(String value);

    void addExamCate(ExamCateReq examCateReq);

    void deleteExamCate(Long id);

    void updateExamCate(ExamCateReq examCateReq);

    List<ExamCateResp> getExamCateList();

    List<ExamCateResp> getFrontExamCateList();

    public List<Long> examIdList(Long cateId);
}
