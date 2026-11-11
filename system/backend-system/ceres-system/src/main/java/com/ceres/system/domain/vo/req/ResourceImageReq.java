package com.ceres.system.domain.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResourceImageReq {

    private String name;

    @NotNull(message = "描述不能为空")
    private String description;

    @NotNull(message = "文件id不能为空")
    private Long fileId;

}
