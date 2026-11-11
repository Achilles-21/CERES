package com.ceres.system.task;

import cn.hutool.core.collection.CollUtil;
import com.ceres.common.constant.CacheConstants;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.mapper.ResourceVideoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class VideoTask {

    @Resource
    private ResourceVideoMapper resourceVideoMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * ?")
    public void videoHitsTask(){
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(CacheConstants.COURSE_VIDEO_HITS_KEY, 0, -1);
        List<ResourceVideo> resourceVideoList = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            ResourceVideo resourceVideo = new ResourceVideo();
            resourceVideo.setId(Long.valueOf(typedTuple.getValue()));
            resourceVideo.setHits(typedTuple.getScore().longValue());
            resourceVideoList.add(resourceVideo);
        }
        if(CollUtil.isNotEmpty(resourceVideoList)){
            resourceVideoMapper.updateBatch(resourceVideoList);
            log.info("同步点击量到数据库执行了");
        }
    }

}
