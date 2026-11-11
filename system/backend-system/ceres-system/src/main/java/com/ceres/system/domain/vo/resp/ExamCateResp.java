package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.ExamCate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ExamCateResp extends ExamCate {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String imageUrl;

    private Long resultId;
}
