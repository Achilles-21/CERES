package com.ceres.system.domain.vo.req;

import lombok.Data;

@Data
public class ResourceImageUpdateReq {

    private Long id;

    private String name;

    private String description;

    private Long fileId;

}
