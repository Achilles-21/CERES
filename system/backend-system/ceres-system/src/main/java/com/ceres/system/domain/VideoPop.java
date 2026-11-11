package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class VideoPop {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer isShow = 1;

    private Long videoId;

    private String queId;

    private String questionTime;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long popVideoId;

}
