package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ceres.system.domain.Exam;
import com.ceres.system.domain.ExamRecord;

import java.util.List;

public interface ExamRecordMapper extends BaseMapper<Exam> {
    void insertRecordList(List<ExamRecord> examRecordList);
}
