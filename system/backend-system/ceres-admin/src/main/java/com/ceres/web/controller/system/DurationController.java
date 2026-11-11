package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IDurationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("duration")
public class DurationController extends BaseController {

    @Resource
    private IDurationService durationService;

    @Log(title = "新增持续时间")
    @GetMapping("add")
    public AjaxResult getReportById(@RequestParam("type") String type, @RequestParam("event") Integer event) {
        durationService.addDuration(type, event, getUserId());
        return AjaxResult.success("添加成功");
    }

    @GetMapping("heatmap")
    public AjaxResult heatmap(@RequestParam(value = "days", required = false, defaultValue = "365") Integer days) {
        return AjaxResult.success(durationService.getHeatmap(getUserId(), days));
    }
}
