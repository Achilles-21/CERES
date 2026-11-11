package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.domain.vo.req.ResourceVideoReq;
import com.ceres.system.domain.vo.req.ResourceVideoUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceVideoAllResp;
import com.ceres.system.domain.vo.resp.ResourceVideoResp;

import java.util.List;

public interface IResourceVideoService {

    void addVideo(ResourceVideoReq req);

    void updateVideo(ResourceVideoUpdateReq req);

    void deleteVideo(Long id);

    ResourceVideoResp getVideoById(Long id);

    PageResult getVideoList(Integer pageNum, Integer pageSize, String name);

    PageResult getVideoListOnShow(Integer pageNum, Integer pageSize, String name);

    List<ResourceVideoAllResp> listAll();

    List<ResourceVideo> selectBatchIds(List<Long> videoPopIds);

    ResourceVideo getById(Long popVideoId);

}
