package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.TableDataInfo;
import com.ceres.system.domain.vo.req.ResourceImageReq;
import com.ceres.system.domain.vo.req.ResourceImageUpdateReq;
import com.ceres.system.domain.vo.resp.ResourceImageResp;
import com.ceres.system.service.IResourceImageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/image")
public class ResourceImageController extends BaseController {

    @Resource
    private IResourceImageService resourceImageService;

    @Log(title = "查询图片列表")
    @GetMapping("list")
    public TableDataInfo list(String name)
    {
        startPage();
        List<ResourceImageResp> list = resourceImageService.getImageList(name);
        return getDataTable(list);
    }

    @Log(title = "根据id查询图片")
    @GetMapping("getImageById")
    public AjaxResult getImageById(@RequestParam("id") Long id) {
        ResourceImageResp video = resourceImageService.getImageById(id);
        return AjaxResult.success(video);
    }

    @Log(title = "新增图片")
    @PostMapping("add")
    public AjaxResult add(@Valid @RequestBody ResourceImageReq video) {
        resourceImageService.addImage(video);
        return AjaxResult.success();
    }

    @Log(title = "修改图片")
    @PostMapping("update")
    public AjaxResult update(@RequestBody ResourceImageUpdateReq video) {
        resourceImageService.updateImage(video);
        return AjaxResult.success();
    }

    @Log(title = "删除图片")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam("id") Long id) {
        resourceImageService.deleteImage(id);
        return AjaxResult.success();
    }

}
