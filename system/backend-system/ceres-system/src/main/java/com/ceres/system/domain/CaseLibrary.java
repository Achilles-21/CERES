package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CaseLibrary {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String patient;

    private Integer questionsNumber;

    private String question1;

    private String answer1;

    private String question2;

    private String answer2;

    private String question3;

    private String answer3;

    private String question4;

    private String answer4;

    private String question5;

    private String answer5;

    private String question6;

    private String answer6;

    private Integer patientImage;

}
