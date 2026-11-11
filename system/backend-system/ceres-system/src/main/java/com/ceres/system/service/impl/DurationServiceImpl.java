package com.ceres.system.service.impl;

import com.ceres.common.utils.DateUtils;
import com.ceres.system.domain.Duration;
import com.ceres.system.domain.vo.resp.DurationHeatmapResp;
import com.ceres.system.domain.vo.stat.DurationHeatmapBuilder;
import com.ceres.system.domain.vo.stat.DurationHeatmapTypeStat;
import com.ceres.system.mapper.DurationMapper;
import com.ceres.system.service.IDurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DurationServiceImpl implements IDurationService {

    @Resource
    private DurationMapper durationMapper;

    @Override
    public void addDuration(String type, Integer event, Long userId) {
        if(event == 0) {
            Duration duration = durationMapper.getLastUserDuration(userId, type);
            if (duration == null) {
                log.warn("No duration session found to close. userId={}, type={}", userId, type);
                return;
            }

            Duration updateDuration = new Duration();
            updateDuration.setId(duration.getId());
            updateDuration.setEndTime(new Date());

            durationMapper.updateById(updateDuration);
        } else {
            Duration duration = new Duration();
            duration.setType(type);
            duration.setUserId(userId);
            duration.setStartTime(new Date());
            duration.setEndTime(new Date());
            duration.setDay(DateUtils.getDate());

            durationMapper.insert(duration);
        }
    }

    @Override
    public DurationHeatmapResp getHeatmap(Long userId, Integer days) {
        int safeDays = DurationHeatmapBuilder.clampDays(days);
        LocalDate endDay = LocalDate.now();
        LocalDate startDay = endDay.minusDays(safeDays - 1L);
        List<DurationHeatmapTypeStat> stats = durationMapper.getHeatmapTypeStats(
                userId,
                startDay.toString(),
                endDay.toString(),
                DurationHeatmapBuilder.REQUIRED_TYPES
        );
        return DurationHeatmapBuilder.build(startDay, endDay, stats);
    }
}
