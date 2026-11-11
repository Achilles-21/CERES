package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.ExamCateRel;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ExamCateRelMapper extends BaseMapper<ExamCateRel> {
    List<ExamCateRel> getExamRelList(@Param("cateId") Long cateId);
    void delExamCateRel(@Param("cateId") Long cateId);
    void setExamCateRel(@Param("queIds") List<Integer> queIds, @Param("cateId") Long cateId);
}
