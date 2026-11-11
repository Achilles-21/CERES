package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.ResourceVideo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceVideoResp extends ResourceVideo {

    private Double size;

    private String videoUrl;

    private String posterUrl;

    private String fileType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Long hits;

    private String content;

    private String value;

}
