package com.ceres.system.domain.vo.req;

import com.ceres.system.domain.Que;
import lombok.Data;

import java.util.List;

@Data
public class QueReq extends Que {

    private List<QueRecordReq> queRecordReqs;

}
