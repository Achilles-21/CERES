package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Ppt {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String pptUrl;

    private String createBy;

    private String delFlag;

    private Date createTime;

    private Long videoId;

}
