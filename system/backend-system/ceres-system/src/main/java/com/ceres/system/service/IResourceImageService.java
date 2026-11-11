package com.ceres.system.service;

import com.ceres.system.domain.vo.req.ResourceImageReq;
import com.ceres.system.domain.vo.req.ResourceImageUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceImageResp;

import java.util.List;

public interface IResourceImageService {

    void addImage(ResourceImageReq req);

    void updateImage(ResourceImageUpdateReq req);

    void deleteImage(Long id);

    ResourceImageResp getImageById(Long id);

    List<ResourceImageResp> getImageList(String name);

}
