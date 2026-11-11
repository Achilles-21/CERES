package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ceres.system.domain.Report;
import org.apache.ibatis.annotations.Param;

public interface ReportMapper extends BaseMapper<Report> {

    IPage<Report> getReportList(@Param("page") IPage<Report> page,
                                @Param("title") String title,
                                @Param("username") String username);

}
