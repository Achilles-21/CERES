package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class RecommendExamRespPage {


    private List<RecommendExamResp> recommendExamRespList;

    private Long total;

    private Long limitTime;

}
