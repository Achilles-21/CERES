package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String answer;

    private Long queId;

    private Long userId;

    private LocalDateTime updateTime;

}
