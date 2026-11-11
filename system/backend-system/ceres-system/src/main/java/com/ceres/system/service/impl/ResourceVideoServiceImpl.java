package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ceres.common.core.page.PageResult;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.domain.FileInfo;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.domain.vo.req.ResourceVideoReq;
import com.ceres.system.domain.vo.req.ResourceVideoUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceVideoAllResp;
import com.ceres.system.domain.vo.resp.ResourceVideoResp;
import com.ceres.system.mapper.FileInfoMapper;
import com.ceres.system.mapper.ResourceVideoMapper;
import com.ceres.system.service.IResourceVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ResourceVideoServiceImpl implements IResourceVideoService {

    @Resource
    private ResourceVideoMapper resourceVideoMapper;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Override
    public void addVideo(ResourceVideoReq req) {
        ResourceVideo video = new ResourceVideo();
        video.setName(req.getName());
        video.setDescription(req.getDescription());
        video.setDuration(req.getDuration());
        video.setFileId(req.getFileId());
        video.setPosterId(req.getPosterId());
        video.setCreateBy(SecurityUtils.getUsername());
        video.setPptUrl(req.getPptUrl());
        resourceVideoMapper.insert(video);
    }

    @Override
    public void updateVideo(ResourceVideoUpdateReq req) {
        ResourceVideo video = new ResourceVideo();
        video.setId(req.getId());
        video.setName(req.getName());
        video.setDescription(req.getDescription());
        video.setDuration(req.getDuration());
        video.setFileId(req.getFileId());
        video.setPosterId(req.getPosterId());
        video.setPptUrl(req.getPptUrl());
        video.setIsShow(req.getIsShow());
        resourceVideoMapper.updateById(video);
    }

    @Override
    public void deleteVideo(Long id) {
        ResourceVideo video = new ResourceVideo();
        video.setId(id);
        video.setDelFlag("2");
        resourceVideoMapper.updateById(video);
    }

    @Override
    public ResourceVideoResp getVideoById(Long id) {
        ResourceVideoResp resp = new ResourceVideoResp();
        ResourceVideo resourceVideo = resourceVideoMapper.selectById(id);
        FileInfo fileInfo = fileInfoMapper.selectById(resourceVideo.getFileId());
        BeanUtils.copyBeanProp(resp, resourceVideo);
        if(resourceVideo.getPosterId()!=null){
            FileInfo info = fileInfoMapper.selectById(resourceVideo.getPosterId());
            if(ObjUtil.isNotEmpty(info)){
                if(ObjUtil.isNotEmpty(info.getFileUrl())){
                    resp.setPosterUrl(info.getFileUrl());
                }
            }
        }
        if(ObjUtil.isNotEmpty(fileInfo)){
            if(StrUtil.isNotBlank(fileInfo.getFileType())){
                resp.setFileType(fileInfo.getFileType());
            }
            Double size = fileInfo.getSize().doubleValue() / (1024 * 1024) * 1.0;
            BigDecimal bigDecimal = new BigDecimal(size);
            resp.setSize(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            if(ObjUtil.isNotEmpty(fileInfo.getFileUrl())){
                resp.setVideoUrl(fileInfo.getFileUrl());
            }
        }
        return resp;
    }

    @Override
    public PageResult getVideoList(Integer pageNum, Integer pageSize, String name) {
        Page<ResourceVideoResp> page = new Page<>(pageNum, pageSize);
        IPage<ResourceVideoResp> list = resourceVideoMapper.getVideoList(name, page);
        for (ResourceVideoResp resourceVideoResp : list.getRecords()) {
            if(resourceVideoResp.getSize()!=null){
                Double size = resourceVideoResp.getSize() / (1024 * 1024) * 1.0;
                BigDecimal bigDecimal = new BigDecimal(size);
                resourceVideoResp.setSize(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        return new PageResult(list.getRecords(), list.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult getVideoListOnShow(Integer pageNum, Integer pageSize, String name) {
        Page<ResourceVideoResp> page = new Page<>(pageNum, pageSize);
        IPage<ResourceVideoResp> list = resourceVideoMapper.getVideoListOnShow(name, page);
        for (ResourceVideoResp resourceVideoResp : list.getRecords()) {
            if(resourceVideoResp.getSize()!=null){
                Double size = resourceVideoResp.getSize() / (1024 * 1024) * 1.0;
                BigDecimal bigDecimal = new BigDecimal(size);
                resourceVideoResp.setSize(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        return new PageResult(list.getRecords(), list.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<ResourceVideoAllResp> listAll() {
        LambdaQueryWrapper<ResourceVideo> lambdaQueryWrapper = new LambdaQueryWrapper<ResourceVideo>().eq(ResourceVideo::getDelFlag, 0);
        List<ResourceVideo> resourceVideoList = resourceVideoMapper.selectList(lambdaQueryWrapper);
        List<ResourceVideoAllResp> resourceVideoAllResps = BeanUtil.copyToList(resourceVideoList, ResourceVideoAllResp.class);
        for (ResourceVideoAllResp resourceVideoAllResp : resourceVideoAllResps) {
            resourceVideoAllResp.setValue(resourceVideoAllResp.getId().toString());
            resourceVideoAllResp.setLabel(resourceVideoAllResp.getName());
        }
        return resourceVideoAllResps;
    }

    @Override
    public List<ResourceVideo> selectBatchIds(List<Long> videoPopIds) {
        List<ResourceVideo> resourceVideoList = resourceVideoMapper.selectBatchIds(videoPopIds);
        return resourceVideoList;
    }

    @Override
    public ResourceVideo getById(Long popVideoId) {
        ResourceVideo resourceVideo = resourceVideoMapper.selectById(popVideoId);
        return resourceVideo;
    }

}
