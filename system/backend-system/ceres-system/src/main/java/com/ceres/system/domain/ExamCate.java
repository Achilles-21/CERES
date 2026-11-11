package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ExamCate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private int auth = 0;

    private Long limitTime;

    private Long imageId;

    private Date createTime;

    private Date updateTime;

}
