package com.ceres.system.domain.vo.resp;

import com.ceres.system.domain.CaseQueBack;
import lombok.Data;

import java.util.List;

@Data
public class CaseAnalysisResp {

    private String role;

    private String content;

    private Integer picture;

    private List<String> imageUrls;

    private Long reportId;

    private Integer questionsNumber;

    private List<CaseQueBack> caseQueBackList;

    private String patient;
}
