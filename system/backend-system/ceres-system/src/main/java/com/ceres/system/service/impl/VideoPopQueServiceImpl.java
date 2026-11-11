package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.domain.FileInfo;
import com.ceres.system.domain.Que;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.domain.VideoPop;
import com.ceres.system.domain.VideoRecord;
import com.ceres.system.domain.vo.resp.VideoPopResp;
import com.ceres.system.mapper.*;
import com.ceres.system.service.IVideoPopQueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoPopQueServiceImpl extends ServiceImpl<VideoPopQueMapper, VideoPop> implements IVideoPopQueService {

    @Resource
    private VideoPopMapper videoPopMapper;
    @Resource
    private QueMapper queMapper;
    @Resource
    private ResourceVideoMapper resourceVideoMapper;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private VideoRecordMapper videoRecordMapper;

    @Override
    public VideoPopResp selectById(Long id) {
        VideoPop videoPop = videoPopMapper.selectById(id);
        VideoPopResp videoPopResp = BeanUtil.toBean(videoPop, VideoPopResp.class);
        ResourceVideo resourceVideo = resourceVideoMapper.selectById(videoPop.getPopVideoId());
        if (resourceVideo != null) {
            videoPopResp.setVideoName(resourceVideo.getName());
            FileInfo info = fileInfoMapper.selectById(resourceVideo.getFileId());
            videoPopResp.setVideoUrl(info.getFileUrl());
        }
        return videoPopResp;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PageResult pageQuery(Integer pageNum, Integer pageSize, Long videoId) {
        Page<VideoPop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VideoPop> lambdaQueryWrapper = new LambdaQueryWrapper<VideoPop>().eq(VideoPop::getVideoId, videoId);
        Page<VideoPop> videoPopPage = videoPopMapper.selectPage(page, lambdaQueryWrapper);
        if (CollUtil.isNotEmpty(videoPopPage.getRecords())) {
            List<VideoPop> videoPopList = videoPopPage.getRecords();
            List<VideoPopResp> videoPopRespList = BeanUtil.copyToList(videoPopList, VideoPopResp.class);
            List<Long> videoPopIds = videoPopList.stream().map(VideoPop::getPopVideoId).collect(Collectors.toList());
            List<ResourceVideo> resourceVideoList = resourceVideoMapper.selectBatchIds(videoPopIds);
            Map<Long, String> videoMap = resourceVideoList.stream().collect(Collectors.toMap(ResourceVideo::getId, ResourceVideo::getName));
            Map<Long, Long> videoFileInfoMap = resourceVideoList.stream().collect(Collectors.toMap(ResourceVideo::getId, ResourceVideo::getFileId));
            List<Long> fileIds = resourceVideoList.stream().map(ResourceVideo::getFileId).collect(Collectors.toList());
            Map<Long, String> fileInfoMap = null;
            if (CollUtil.isNotEmpty(fileIds)) {
                List<FileInfo> fileInfoList = fileInfoMapper.selectBatchIds(fileIds);
                fileInfoMap = fileInfoList.stream().collect(Collectors.toMap(FileInfo::getId, FileInfo::getFileUrl));
            }
            for (VideoPopResp videoPopResp : videoPopRespList) {
                if (StrUtil.isNotBlank(videoMap.get(videoPopResp.getPopVideoId()))) {
                    videoPopResp.setVideoName(videoMap.get(videoPopResp.getPopVideoId()));
                }
                if (fileInfoMap != null && videoFileInfoMap.get(videoPopResp.getPopVideoId()) != null) {
                    videoPopResp.setVideoUrl(fileInfoMap.get(videoFileInfoMap.get(videoPopResp.getPopVideoId())));
                }
            }
            return new PageResult(videoPopRespList, videoPopPage.getTotal(), (int) videoPopPage.getCurrent(), (int) videoPopPage.getSize());
        }
        return new PageResult(ListUtil.empty(), videoPopPage.getTotal(), (int) videoPopPage.getCurrent(), (int) videoPopPage.getSize());
    }

    @Override
    public void setVideoRecord(List<VideoRecord> videoRecordList) {
        for (VideoRecord videoRecord : videoRecordList) {
            videoRecord.setUserId(SecurityUtils.getUserId());
            videoRecord.setCreateTime(LocalDateTime.now());
        }
        videoRecordMapper.saveBatchList(videoRecordList);
    }

    @Override
    public Boolean getIsAnswered(Long popId) {
        LambdaQueryWrapper<VideoRecord> wrapper = new LambdaQueryWrapper<VideoRecord>().eq(VideoRecord::getUserId, SecurityUtils.getUserId()).eq(VideoRecord::getPopId, popId);
        return videoRecordMapper.exists(wrapper);
    }
}
