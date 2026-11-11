package com.ceres.system.service.impl;

import com.ceres.common.utils.StringUtils;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.mapper.ResourceVideoMapper;
import com.ceres.system.service.IPptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PptServiceImpl implements IPptService {

    @Resource
    private ResourceVideoMapper resourceVideoMapper;

    @Override
    public String getPPTFile(Long videoId) {
        ResourceVideo resourceVideo = resourceVideoMapper.selectById(videoId);
        if (StringUtils.isNull(resourceVideo.getPptUrl())) {
            log.info("视频ID为：{}，没有找到对应的PPT文件", videoId);
            return "";
        }
        return resourceVideo.getPptUrl();
    }
}
