package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Que {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String type = "0";

    private String answerExplain;

    private String description;

    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String choiceE;
    private String choiceF;

    private String status;

    private String delFlag;

    private String content;

    private String imgUrl;

    private String imgId;

    private String answer;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

}
