package com.ceres.system.domain.vo.stat;

import com.ceres.system.domain.vo.resp.DurationHeatmapDayResp;
import com.ceres.system.domain.vo.resp.DurationHeatmapResp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DurationHeatmapBuilder {

    public static final List<String> REQUIRED_TYPES = Arrays.asList("class", "exercise", "test", "note", "discuss", "case");
    private static final Set<String> REQUIRED_TYPE_SET = REQUIRED_TYPES.stream().collect(Collectors.toSet());
    private static final int FREQUENCY_TARGET = 12;

    private DurationHeatmapBuilder() {
    }

    public static int clampDays(Integer days) {
        if (days == null || days < 1) {
            return 1;
        }
        return Math.min(days, 366);
    }

    public static DurationHeatmapResp build(LocalDate startDay, LocalDate endDay, List<DurationHeatmapTypeStat> stats) {
        Map<String, Map<String, Integer>> countsByDay = buildCountsByDay(stats);
        DurationHeatmapResp response = new DurationHeatmapResp();
        response.setStartDay(startDay.toString());
        response.setEndDay(endDay.toString());
        response.setTotalTypes(REQUIRED_TYPES.size());

        List<DurationHeatmapDayResp> days = new ArrayList<>();
        int activeDays = 0;
        int currentRun = 0;
        int maxStreak = 0;
        int totalSessions = 0;

        for (LocalDate cursor = startDay; !cursor.isAfter(endDay); cursor = cursor.plusDays(1)) {
            String day = cursor.toString();
            DurationHeatmapDayResp dayResp = buildDay(day, countsByDay.get(day));
            days.add(dayResp);
            totalSessions += dayResp.getSessionCount();

            if (dayResp.getSessionCount() > 0) {
                activeDays += 1;
                currentRun += 1;
                maxStreak = Math.max(maxStreak, currentRun);
            } else {
                currentRun = 0;
            }
        }

        response.setActiveDays(activeDays);
        response.setCurrentStreak(currentRun);
        response.setMaxStreak(maxStreak);
        response.setTotalSessions(totalSessions);
        response.setDays(days);
        return response;
    }

    private static Map<String, Map<String, Integer>> buildCountsByDay(List<DurationHeatmapTypeStat> stats) {
        if (stats == null) {
            return new LinkedHashMap<>();
        }
        return stats.stream()
                .filter(Objects::nonNull)
                .filter(stat -> stat.getDay() != null && REQUIRED_TYPE_SET.contains(stat.getType()))
                .collect(Collectors.groupingBy(
                        DurationHeatmapTypeStat::getDay,
                        LinkedHashMap::new,
                        Collectors.toMap(
                                DurationHeatmapTypeStat::getType,
                                stat -> stat.getSessionCount() == null ? 0 : stat.getSessionCount(),
                                Integer::sum,
                                LinkedHashMap::new
                        )
                ));
    }

    private static DurationHeatmapDayResp buildDay(String day, Map<String, Integer> sourceCounts) {
        Map<String, Integer> typeCounts = REQUIRED_TYPES.stream()
                .collect(Collectors.toMap(Function.identity(), type -> 0, (left, right) -> left, LinkedHashMap::new));
        if (sourceCounts != null) {
            sourceCounts.forEach((type, count) -> {
                if (REQUIRED_TYPE_SET.contains(type)) {
                    typeCounts.put(type, Math.max(count == null ? 0 : count, 0));
                }
            });
        }

        int sessionCount = typeCounts.values().stream().mapToInt(Integer::intValue).sum();
        int typeCount = (int) typeCounts.values().stream().filter(count -> count > 0).count();
        double score = calculateScore(typeCount, sessionCount);

        DurationHeatmapDayResp dayResp = new DurationHeatmapDayResp();
        dayResp.setDay(day);
        dayResp.setLevel(calculateLevel(typeCount, sessionCount, score));
        dayResp.setScore(score);
        dayResp.setTypeCount(typeCount);
        dayResp.setSessionCount(sessionCount);
        dayResp.setTypeCounts(typeCounts);
        return dayResp;
    }

    private static double calculateScore(int typeCount, int sessionCount) {
        if (sessionCount <= 0) {
            return 0.0;
        }
        double diversityScore = typeCount / (double) REQUIRED_TYPES.size();
        double frequencyScore = Math.min(sessionCount / (double) FREQUENCY_TARGET, 1.0);
        return (0.72 * diversityScore) + (0.28 * frequencyScore);
    }

    private static int calculateLevel(int typeCount, int sessionCount, double score) {
        if (sessionCount <= 0) {
            return 0;
        }
        if (typeCount == REQUIRED_TYPES.size()) {
            return 4;
        }
        if (score < 0.30) {
            return 1;
        }
        if (score < 0.60) {
            return 2;
        }
        if (score < 0.85) {
            return 3;
        }
        return 4;
    }
}
