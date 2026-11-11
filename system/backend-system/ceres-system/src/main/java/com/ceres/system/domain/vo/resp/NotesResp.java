package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.Notes;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class NotesResp extends Notes {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
