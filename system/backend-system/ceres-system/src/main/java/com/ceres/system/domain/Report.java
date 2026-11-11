package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Report {
    private String content;
    private String createBy;
    private Date createTime;
    private String delFlag;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;

    private Long videoId;
}
