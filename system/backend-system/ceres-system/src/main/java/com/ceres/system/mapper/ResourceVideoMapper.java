package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ceres.system.domain.ResourceVideo;
import com.ceres.system.domain.vo.resp.ResourceVideoResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceVideoMapper extends BaseMapper<ResourceVideo> {


    IPage<ResourceVideoResp> getVideoList(@Param("name") String name,
                                          @Param("page") IPage<ResourceVideoResp> page);


    IPage<ResourceVideoResp> getVideoListOnShow(@Param("name") String name,
    @Param("page") IPage<ResourceVideoResp> page);

    void updateBatch(@Param("resourceVideoList") List<ResourceVideo> resourceVideoList);

    void updateBatchPptUrl(@Param("resourceVideoList") List<ResourceVideo> resourceVideoList);

}
