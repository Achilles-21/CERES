package com.ceres.system.domain.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResp {

    private String role;

    private String content;

    private Integer picture;

    private Integer up = 0;

    private Long historyId;

    /** Video flag: 1 = correct answer, no popup; 0 = show popup. */
    private String isPop;

    private String videoUrl;

    private Long courseId;

    private List<RecommendQueResp> recommendQueRespList;

    private Map<String, Object> topics;

}
