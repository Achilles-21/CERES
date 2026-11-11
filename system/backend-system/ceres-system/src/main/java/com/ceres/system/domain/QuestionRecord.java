package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long queId;

    private String queContent;

    private Long userId;

    private String userOptions;

    /** Answer result: 1 = correct, 0 = incorrect. */
    private Integer result;

    private LocalDateTime createTime;

}
