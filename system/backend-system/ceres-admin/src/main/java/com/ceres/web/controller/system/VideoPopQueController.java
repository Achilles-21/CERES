package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.VideoPop;
import com.ceres.system.domain.VideoRecord;
import com.ceres.system.domain.vo.resp.VideoPopResp;
import com.ceres.system.service.IVideoPopQueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/videoPopQue")
public class VideoPopQueController {

    @Resource
    private IVideoPopQueService videoPopQueService;

    @Log(title = "新增课程弹窗题目")
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody VideoPop videoPop) {
        videoPopQueService.save(videoPop);
        return AjaxResult.success();
    }

    @Log(title = "根据id查询课程弹窗题目")
    @GetMapping("/getById")
    public AjaxResult getVideoById(@RequestParam("id") Long id) {
        VideoPopResp videoPopResp = videoPopQueService.selectById(id);
        return AjaxResult.success(videoPopResp);
    }

    @Log(title = "修改课程弹窗题目")
    @PostMapping("/update")
    public AjaxResult update(@RequestBody VideoPop videoPop) {
        videoPopQueService.updateById(videoPop);
        return AjaxResult.success();
    }

    @Log(title = "分页查询课程弹窗题目")
    @GetMapping("/page")
    public AjaxResult pageQuery(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize, @RequestParam("videoId") Long videoId) {
        PageResult pageResult =  videoPopQueService.pageQuery(pageNum, pageSize, videoId);
        return AjaxResult.success(pageResult);
    }

    @Log(title = "删除课程弹窗题目")
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        videoPopQueService.removeById(id);
        return AjaxResult.success();
    }

    @Log(title = "查询用户观看视频记录")
    @PostMapping("/setVideoRecord")
    public AjaxResult setVideoRecord(@RequestBody List<VideoRecord> videoRecordList) {
        videoPopQueService.setVideoRecord(videoRecordList);
        return AjaxResult.success();
    }
}
