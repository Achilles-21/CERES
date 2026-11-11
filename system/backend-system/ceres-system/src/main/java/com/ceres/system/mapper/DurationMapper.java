package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.Duration;
import com.ceres.system.domain.vo.stat.DurationHeatmapTypeStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DurationMapper extends BaseMapper<Duration> {

    Duration getLastUserDuration(@Param("userId") Long userId, @Param("type") String type);

    List<DurationHeatmapTypeStat> getHeatmapTypeStats(
            @Param("userId") Long userId,
            @Param("startDay") String startDay,
            @Param("endDay") String endDay,
            @Param("types") List<String> types
    );

}
