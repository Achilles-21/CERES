package com.ceres.system.domain.vo.req;

import lombok.Data;

@Data
public class ResourceVideoUpdateReq {

    private Long id;

    private String name;

    private String description;

    private Long fileId;

    private Long posterId;

    private Double duration;

    private String questionTime;

    private Long queId;

    private String pptUrl;

    private Integer isShow;

}
