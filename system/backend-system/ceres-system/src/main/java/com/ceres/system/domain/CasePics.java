package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CasePics {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long caseId;

    private String pic;

}
