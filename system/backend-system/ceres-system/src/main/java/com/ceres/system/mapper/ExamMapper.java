package com.ceres.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.Exam;

public interface ExamMapper extends BaseMapper<Exam> {
    List<Exam> selectListByCateId(@Param("cateId") Long cateId);
}
