package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String role;

    private String text;

    private Integer up = 0;

    private LocalDateTime timeSpan;

}
