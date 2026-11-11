package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseResp {

    private Long user_id;

    private Long course_id;

    private List<QueResponse> questions;

}
