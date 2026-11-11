package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.CaseLibrary;
import com.ceres.system.domain.CasePics;
import lombok.Data;

import java.util.List;

@Data
public class CaseResp extends CaseLibrary {

    private List<CasePics> casePicsList;

}
