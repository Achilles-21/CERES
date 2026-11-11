package com.ceres.system.domain.vo.req;

import lombok.Data;

@Data
public class TeacherImageReq {

    private Long id;

    private Long videoId;

    private String teacherImage;

    private Integer imageTime;

}
