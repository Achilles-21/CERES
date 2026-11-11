package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ResourceVideo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Double duration;

    private Long fileId;

    private String name;

    private Date createTime;

    private String createBy;

    private String description;

    private Long posterId;

    private String delFlag;

    private Long hits;

    private String pptUrl;

    private Integer isShow = 1;

}
