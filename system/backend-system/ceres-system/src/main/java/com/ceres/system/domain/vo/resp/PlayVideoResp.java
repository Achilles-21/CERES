package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class PlayVideoResp extends ResourceVideoResp {
    private Double playingTime;

    private List<TeacherImageResp> teacherImageRespList;

    private List<VideoPopResp> videoPopRespList;

}
