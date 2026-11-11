package com.ceres.system.enums;

public enum ChatRoleEnum {

    SYSTEM, USER, ASSISTANT;

    public String value() { return name().toLowerCase(); }

}
