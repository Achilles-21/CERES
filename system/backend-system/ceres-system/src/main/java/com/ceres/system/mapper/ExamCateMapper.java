package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.ExamCate;
import com.ceres.system.domain.vo.resp.ExamCateResp;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ExamCateMapper extends BaseMapper<ExamCate> {
    List<ExamCateResp> getExamCateList(Long userId);
    List<ExamCateResp> getFrontExamCateList(@Param("userId") Long userId, @Param("level") String level);
}
