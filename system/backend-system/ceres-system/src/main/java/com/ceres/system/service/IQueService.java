package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.Que;
import com.ceres.system.domain.vo.req.QueReq;
import com.ceres.system.domain.vo.resp.QueResp;
import com.ceres.system.domain.vo.resp.RecommendQueResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IQueService {

    void addQue(QueReq req);

    void deleteQue(Long id);

    void updateQue(QueReq req);

    void updateQueStatus(Long id, String status);

    PageResult getQueList(Integer pageNum, Integer pageSize, String type, String content);

    QueResp getQueById(Long id);

    List<RecommendQueResp> getRecommendQueList();

    void saveQueRecord(QueReq req);

    PageResult getQueRecordList(Integer pageNum, Integer pageSize);

    PageResult getAnswer(Integer pageNum, Integer pageSize);

    List<RecommendQueResp> getAnswerDetails(Long userId);

    List<Map<String, String>> listAll(String value);

    Map<String, String> send(Map<String, String> map);

    List<Que> selectBatchIds(List<Long> queIds);

    List<RecommendQueResp> getQueByIds(ArrayList<Long> questionIds);
}
