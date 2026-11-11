package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DurationHeatmapResp {

    private String startDay;
    private String endDay;
    private Integer totalTypes;
    private Integer activeDays;
    private Integer currentStreak;
    private Integer maxStreak;
    private Integer totalSessions;
    private List<DurationHeatmapDayResp> days = new ArrayList<>();
}
