package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Invitation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content;


    private Integer comments;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** Deletion flag: 0 = active (default), 1 = deleted. */
    private Integer isDeleted;

}
