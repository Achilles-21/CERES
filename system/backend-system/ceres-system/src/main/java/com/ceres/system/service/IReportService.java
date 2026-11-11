package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.Report;
import com.ceres.system.domain.vo.resp.ReportResp;

import java.util.List;

public interface IReportService {

    PageResult getReportList(Integer pageNum, Integer pageSize, String title);

    ReportResp getReportById(Long id);

    void deleteReport(Long id);

    void addReport(Report report);

    ReportResp getReportByVideoId(Long videoId);

}
