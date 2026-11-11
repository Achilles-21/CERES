package com.ceres.system.service.impl;

import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.domain.FileInfo;
import com.ceres.system.domain.ResourceImage;
import com.ceres.system.domain.vo.req.ResourceImageReq;
import com.ceres.system.domain.vo.req.ResourceImageUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceImageResp;
import com.ceres.system.mapper.FileInfoMapper;
import com.ceres.system.mapper.ResourceImageMapper;
import com.ceres.system.service.IResourceImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ResourceImageServiceImpl implements IResourceImageService {

    @Resource
    private ResourceImageMapper resourceImageMapper;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Override
    public void addImage(ResourceImageReq req) {
        ResourceImage resourceImage = new ResourceImage();
        BeanUtils.copyProperties(req, resourceImage);
        resourceImage.setCreateBy(SecurityUtils.getUsername());
        resourceImageMapper.insert(resourceImage);
    }

    @Override
    public void updateImage(ResourceImageUpdateReq req) {
        ResourceImage resourceImage = new ResourceImage();
        BeanUtils.copyProperties(req, resourceImage);
        resourceImageMapper.updateById(resourceImage);
    }

    @Override
    public void deleteImage(Long id) {
        ResourceImage resourceImage = new ResourceImage();
        resourceImage.setId(id);
        resourceImage.setDelFlag("2");
        resourceImageMapper.updateById(resourceImage);
    }

    @Override
    public ResourceImageResp getImageById(Long id) {
        ResourceImageResp resourceImageResp = new ResourceImageResp();
        ResourceImage resourceImage = resourceImageMapper.selectById(id);
        BeanUtils.copyProperties(resourceImage, resourceImageResp);
        FileInfo fileInfo = fileInfoMapper.selectById(resourceImage.getFileId());
        resourceImageResp.setFileId(fileInfo.getId());
        resourceImageResp.setFileType(fileInfo.getFileType());
        resourceImageResp.setImageUrl(fileInfo.getFileUrl());
        Double size = fileInfo.getSize().doubleValue() / (1024 * 1024) * 1.0;
        BigDecimal bigDecimal = new BigDecimal(size);
        resourceImageResp.setSize(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        return resourceImageResp;
    }

    @Override
    public List<ResourceImageResp> getImageList(String name) {
        List<ResourceImageResp> list = resourceImageMapper.selectImageList(name);
        if (list != null) {
            for (ResourceImageResp resourceImageResp : list) {
                Double size = resourceImageResp.getSize() / (1024 * 1024) * 1.0;
                BigDecimal bigDecimal = new BigDecimal(size);
                resourceImageResp.setSize(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        return list;
    }
}
