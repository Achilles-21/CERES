package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ceres.system.domain.CourseRecord;
import com.ceres.system.domain.vo.resp.CourseRecordListResp;
import org.apache.ibatis.annotations.Param;

public interface CourseRecordMapper extends BaseMapper<CourseRecord> {

    IPage<CourseRecordListResp> getCourseRecordList(@Param("page") IPage<CourseRecordListResp> page,
                                                    @Param("userId") Long userId);
}
