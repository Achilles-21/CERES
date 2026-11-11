package com.ceres.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleTime {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String moduleType;

    private LocalDate usedDate;

    private Integer duration;

}
