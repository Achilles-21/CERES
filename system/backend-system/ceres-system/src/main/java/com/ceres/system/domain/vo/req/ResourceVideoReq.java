package com.ceres.system.domain.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResourceVideoReq {

    private String name;

    @NotNull(message = "描述不能为空")
    private String description;

    @NotNull(message = "文件id不能为空")
    private Long fileId;

    private Long posterId;

    @NotNull(message = "时长不能为空")
    private Double duration;

    private String questionTime;

    private Long queId;

    private String pptUrl;

}
