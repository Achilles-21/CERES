package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ExamRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String cateId;

    private String answer;

    private Long examId;

    private Long userId;

    private Date updateTime;

}
