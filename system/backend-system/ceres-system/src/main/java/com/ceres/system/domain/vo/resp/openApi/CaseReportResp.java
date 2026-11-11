package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class CaseReportResp {

    private Long idx;

    private String label;

    private String context;

    private Integer n_qa;

    private List<QAResp> qa;

}
