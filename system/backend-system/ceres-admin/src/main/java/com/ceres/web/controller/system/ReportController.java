package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("report")
public class ReportController {

    @Resource
    private IReportService reportService;

    @Log(title = "分页查询报告")
    @GetMapping("getReportList")
    public AjaxResult getReportList(@RequestParam("pageNum") Integer pageNum,
                                    @RequestParam("pageSize") Integer pageSize,
                                    String title) {
        return AjaxResult.success(reportService.getReportList(pageNum, pageSize, title));
    }

    @Log(title = "删除报告")
    @GetMapping("deleteReport")
    public AjaxResult deleteReport(@RequestParam("id") Long id) {
        reportService.deleteReport(id);
        return AjaxResult.success();
    }

    @Log(title = "根据id查询报告")
    @GetMapping("getReportById")
    public AjaxResult getReportById(@RequestParam("id") Long id) {
        return AjaxResult.success(reportService.getReportById(id));
    }

    @Log(title = "根据课程查询报告")
    @GetMapping("getReportByVideoId")
    public AjaxResult getReportByVideoId(@RequestParam("videoId") Long videoId) {
        return AjaxResult.success(reportService.getReportByVideoId(videoId));
    }

}
