package com.ceres.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.VideoRecord;

public interface VideoRecordMapper extends BaseMapper<VideoRecord> {
    int saveBatchList(@Param("videoRecordList") List<VideoRecord> videoRecordList);
}
