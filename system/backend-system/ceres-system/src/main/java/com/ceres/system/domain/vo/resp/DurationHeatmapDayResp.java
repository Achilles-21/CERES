package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DurationHeatmapDayResp {

    private String day;
    private Integer level;
    private Double score;
    private Integer typeCount;
    private Integer sessionCount;
    private Map<String, Integer> typeCounts = new LinkedHashMap<>();
}
