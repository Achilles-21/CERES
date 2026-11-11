package com.ceres.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.VideoPop;
import com.ceres.system.domain.VideoRecord;
import com.ceres.system.domain.vo.resp.VideoPopResp;

public interface IVideoPopQueService extends IService<VideoPop> {

    VideoPopResp selectById(Long id);

    @SuppressWarnings("rawtypes")
    PageResult pageQuery(Integer pageNum, Integer pageSize, Long videoId);

    void setVideoRecord(List<VideoRecord> videoRecordList);

    public Boolean getIsAnswered(Long popId);

}
