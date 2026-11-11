package com.ceres.system.domain.vo.req;

import com.ceres.system.domain.CaseLibrary;
import lombok.Data;

import java.util.List;

@Data
public class CaseReq extends CaseLibrary {

    private List<String> casePicsList;

}
