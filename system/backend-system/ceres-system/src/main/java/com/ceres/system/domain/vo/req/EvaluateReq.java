package com.ceres.system.domain.vo.req;

import lombok.Data;

@Data
public class EvaluateReq {

    private String question;

    private String answer;

    private String studentAnswers;

    private String patient;

}
