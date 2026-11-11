package com.ceres.system.domain.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class ExamRecordListReq {

    private List<ExamRecordReq> examRecordReqs;

    private String cateId;

}
