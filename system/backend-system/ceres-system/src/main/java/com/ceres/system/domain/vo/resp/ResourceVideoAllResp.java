package com.ceres.system.domain.vo.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class ResourceVideoAllResp {

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

    private String value;

    private String label;

}
