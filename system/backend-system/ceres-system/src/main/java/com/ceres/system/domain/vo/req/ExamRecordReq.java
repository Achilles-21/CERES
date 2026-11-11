package com.ceres.system.domain.vo.req;

import com.ceres.system.domain.ExamRecord;
import lombok.Data;

@Data
public class ExamRecordReq extends ExamRecord {

    private Long examId;

    private String answer;

}
