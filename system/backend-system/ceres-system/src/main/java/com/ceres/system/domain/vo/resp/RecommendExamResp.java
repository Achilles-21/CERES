package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class RecommendExamResp {

    private Long queId;


    private String answerExplain;

    private List<ChoiceOptions> options;

    private String content;

    private String imgUrl;

    private String type;

    private String imgId;

    private String answer;

    private String favoriteFlag;

    private String isanswer;

    private Boolean disabled = false;

}
