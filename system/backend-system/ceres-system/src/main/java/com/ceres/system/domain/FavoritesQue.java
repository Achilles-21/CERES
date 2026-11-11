package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class FavoritesQue {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long queId;

    private Date createTime;

    private String createBy;

    public FavoritesQue() {
    }

    public FavoritesQue(Long userId, Long queId) {
        this.userId = userId;
        this.queId = queId;
    }

}
