package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.domain.vo.req.ResourceVideoReq;
import com.ceres.system.domain.vo.req.ResourceVideoUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceVideoAllResp;
import com.ceres.system.domain.vo.resp.ResourceVideoResp;
import com.ceres.system.service.IResourceVideoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("video")
public class ResourceVideoController extends BaseController {

    @Resource
    private IResourceVideoService resourceVideoService;

    @Log(title = "分页查询课程")
    @GetMapping("list")
    public AjaxResult list(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize, @RequestParam(value = "name", required = false) String name) {
        return AjaxResult.success(resourceVideoService.getVideoList(pageNum, pageSize, name));
    }

    @Log(title = "根据id查询课程")
    @GetMapping("getVideoById")
    public AjaxResult getVideoById(@RequestParam("id") Long id) {
        ResourceVideoResp video = resourceVideoService.getVideoById(id);
        return AjaxResult.success(video);
    }

    @Log(title = "新增课程")
    @PostMapping("add")
    public AjaxResult add(@Valid @RequestBody ResourceVideoReq video) {
        resourceVideoService.addVideo(video);
        return AjaxResult.success();
    }

    @Log(title = "修改课程")
    @PostMapping("update")
    public AjaxResult update(@RequestBody ResourceVideoUpdateReq video) {
        resourceVideoService.updateVideo(video);
        return AjaxResult.success();
    }

    @Log(title = "删除课程")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam("id") Long id) {
        resourceVideoService.deleteVideo(id);
        return AjaxResult.success();
    }

    @Log(title = "查询所有课程")
    @GetMapping("/listAll")
    public AjaxResult listAll(){
        List<ResourceVideoAllResp> resourceVideoList = resourceVideoService.listAll();
        return success(resourceVideoList);
    }

}
