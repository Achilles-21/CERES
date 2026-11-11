package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ExamResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String cateId;

    private Long userId;

    private int score;

    private Date updateTime;

}
