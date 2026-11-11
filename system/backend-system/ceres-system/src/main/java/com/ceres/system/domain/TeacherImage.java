package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherImage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long videoId;

    private String teacherImage;

    private LocalDateTime createTime;

    private String createBy;

    private Integer imageTime;

}
