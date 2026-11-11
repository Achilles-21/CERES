package com.ceres.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ceres.system.domain.QueRecord;
import com.ceres.system.domain.vo.resp.QueAnswerRecordResp;
import org.apache.ibatis.annotations.Param;

public interface QueRecordMapper extends BaseMapper<QueRecord> {

    IPage<QueAnswerRecordResp> getQueRecordList(@Param("page") IPage<QueAnswerRecordResp> page);

}
