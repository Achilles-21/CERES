package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.Ppt;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PptResp extends Ppt {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
