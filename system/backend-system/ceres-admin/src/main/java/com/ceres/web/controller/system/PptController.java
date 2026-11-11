package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.IPptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("ppt")
public class PptController {

    @Resource
    private IPptService ptService;

    @Log(title = "查询ppt")
    @GetMapping("getPPTFile")
    public AjaxResult getPPTFile(@RequestParam("videoId") Long videoId) {
        String pptUrl = ptService.getPPTFile(videoId);
        return AjaxResult.success(null, pptUrl);
    }

}
