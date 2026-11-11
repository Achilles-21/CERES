package com.ceres.system.domain.vo.resp.openApi;

import lombok.Data;

import java.util.List;

@Data
public class ExampleAnalysisResp {

    private Long report_id;

    private String concept;

    private String context;

    private List<String> image_urls;

    private String answer;

    private Integer patientImage;

}
