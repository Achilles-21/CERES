package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class QueResponse {

    private Long question_id;

    private String question;

    /** Preserve option order: A, B, C, D, E, F. */
    private List<String> options;

    private Integer answer;

    private String solution;

    private List<String> image_urls;

    private Long user_ans;

}
