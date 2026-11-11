package com.ceres.system.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.constant.CacheConstants;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.system.domain.CourseRecord;
import com.ceres.system.domain.vo.resp.CourseRecordListResp;
import com.ceres.system.mapper.CourseRecordMapper;
import com.ceres.system.service.ICourseRecordService;
import com.ceres.system.service.IResourceVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CourseRecordServiceImpl implements ICourseRecordService {

    @Resource
    private CourseRecordMapper courseRecordMapper;

    @Resource
    private IResourceVideoService resourceVideoService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public CourseRecord getCourseRecordById(Long videoId) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<CourseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseRecord::getUserId, userId)
                .eq(CourseRecord::getVideoId, videoId)
                .last("limit 1");
        return courseRecordMapper.selectOne(wrapper);
    }

    @Override
    public void updateCoursePlayTime(Long videoId, Double playingTime) {
        Long userId = SecurityUtils.getUserId();
        CourseRecord courseRecordById = getCourseRecordById(videoId);
        if (ObjUtil.isEmpty(courseRecordById)) {
            courseRecordById = new CourseRecord();
            courseRecordById.setUserId(userId);
            courseRecordById.setVideoId(videoId);
            courseRecordById.setPlayingTime(playingTime);
            courseRecordMapper.insert(courseRecordById);
        }else {
            courseRecordById.setPlayingTime(playingTime);
            courseRecordMapper.updateById(courseRecordById);
        }
    }

    @Override
    public Double playVideo(Long videoId) {
        Long userId = SecurityUtils.getUserId();
        CourseRecord courseRecordById = getCourseRecordById(videoId);
        if (StringUtils.isNull(courseRecordById)) {
            courseRecordById = new CourseRecord();
            courseRecordById.setUserId(userId);
            courseRecordById.setVideoId(videoId);
            courseRecordById.setPlayingTime(0.0);
            courseRecordMapper.insert(courseRecordById);
        }
        return courseRecordById.getPlayingTime();
    }

    @Override
    public PageResult getCourseRecordList(Integer pageNum, Integer pageSize) {
        Page<CourseRecordListResp> page = new Page<>(pageNum, pageSize);
        IPage<CourseRecordListResp> pageResult = courseRecordMapper.getCourseRecordList(page, SecurityUtils.getUserId());
        return new PageResult(pageResult.getRecords(), pageResult.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult getCourseList(Integer pageNum, Integer pageSize) {

        return resourceVideoService.getVideoListOnShow(pageNum, pageSize, null);
    }


    @Override
    public void hits(Long id) {
        redisTemplate.opsForZSet().incrementScore(CacheConstants.COURSE_VIDEO_HITS_KEY, id.toString(), 1);
    }

}
