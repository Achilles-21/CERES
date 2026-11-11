package com.ceres.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModeCodeEnum {

    QUESTION(0, "普通对话框问答"),
    TEST_REQUEST(1, "申请练习题"),
    CASE_REPORT_REQUEST(2, "学生请求做病例分析练习"),
    CASE_REPORT_PATIENT(3, "病例分析里面模拟患者和学生对话"),
    CASE_REPORT_DOCTOR(4, "学生开始回答病例相关的问题");

    private final Integer code;
    private final String value;

}
