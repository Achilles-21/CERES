package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class NotesResponse {

    private Long user_id;

    private Long course_id;

    private List<String> image_urls;

    private String note_text;

}
