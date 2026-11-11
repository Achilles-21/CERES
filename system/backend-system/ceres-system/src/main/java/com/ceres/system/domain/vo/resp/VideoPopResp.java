package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.Que;
import com.ceres.system.domain.VideoPop;
import lombok.Data;

import java.util.List;

@Data
public class VideoPopResp extends VideoPop {

    private String content;

    private String answer;

    private String videoName;

    private String videoUrl;

    private Boolean isAnswer = false;

    private List<Que> queList;

}
