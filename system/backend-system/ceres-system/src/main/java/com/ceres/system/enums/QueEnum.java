package com.ceres.system.enums;

public enum QueEnum {

    QUE_TYPE_MULTIPLE("1", "多选题"),
    QUE_TYPE_SINGLE("0", "单选题");

    private String code;
    private String desc;

    QueEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
