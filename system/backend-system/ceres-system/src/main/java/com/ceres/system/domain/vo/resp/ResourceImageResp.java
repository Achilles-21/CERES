package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.ResourceImage;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ResourceImageResp extends ResourceImage {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String imageUrl;

    private Double size;

    private String fileType;

}
