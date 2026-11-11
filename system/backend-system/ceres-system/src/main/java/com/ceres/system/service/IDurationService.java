package com.ceres.system.service;

import com.ceres.system.domain.vo.resp.DurationHeatmapResp;

public interface IDurationService {

    void addDuration(String type, Integer event, Long userId);

    DurationHeatmapResp getHeatmap(Long userId, Integer days);

}
