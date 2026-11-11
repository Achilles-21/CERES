package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.VideoPop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoPopMapper extends BaseMapper<VideoPop> {

    int saveBatchList(@Param("videoPopList") List<VideoPop> videoPopList);

}
