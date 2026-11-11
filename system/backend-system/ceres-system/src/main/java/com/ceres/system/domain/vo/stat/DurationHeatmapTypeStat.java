package com.ceres.system.domain.vo.stat;

import lombok.Data;

@Data
public class DurationHeatmapTypeStat {

    private String day;
    private String type;
    private Integer sessionCount;
}
