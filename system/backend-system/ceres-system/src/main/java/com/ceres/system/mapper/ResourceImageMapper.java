package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.ResourceImage;
import com.ceres.system.domain.vo.resp.ResourceImageResp;

import java.util.List;

public interface ResourceImageMapper extends BaseMapper<ResourceImage> {

    List<ResourceImageResp> selectImageList(String name);

}
