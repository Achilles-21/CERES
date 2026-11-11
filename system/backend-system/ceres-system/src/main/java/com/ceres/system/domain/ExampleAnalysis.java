package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ExampleAnalysis {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String concept;

    private String context;

    private String answer;

}
