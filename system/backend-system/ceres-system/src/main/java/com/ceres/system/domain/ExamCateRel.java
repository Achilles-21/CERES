package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ExamCateRel {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long cateId;

    private Date createTime;

}
