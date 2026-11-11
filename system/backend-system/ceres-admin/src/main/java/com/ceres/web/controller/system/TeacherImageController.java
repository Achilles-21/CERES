package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.domain.vo.req.TeacherImageReq;
import com.ceres.system.domain.vo.resp.TeacherImageResp;
import com.ceres.system.service.ITeacherImageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/teacher/image")
public class TeacherImageController extends BaseController {

    @Resource
    private ITeacherImageService teacherImageService;

    @Log(title = "新增教师形象")
    @PostMapping("/add")
    public AjaxResult addTeacherImage(@RequestBody TeacherImageReq teacherImageReq){
        teacherImageService.addTeacherImage(teacherImageReq);
        return success();
    }

    @Log(title = "根据id查询教师形象")
    @GetMapping("/{id}")
    public AjaxResult getTeacherImageById(@PathVariable Long id){
        TeacherImageResp teacherImageResp = teacherImageService.getTeacherImageById(id);
        return success(teacherImageResp);
    }

    @Log(title = "修改教师形象")
    @PutMapping("/update")
    public AjaxResult updateTeacherImage(@RequestBody TeacherImageReq teacherImageReq){
        teacherImageService.updateTeacherImage(teacherImageReq);
        return success();
    }

    @Log(title = "查询当前课程的教师形象")
    @GetMapping("/list/{videoId}")
    public AjaxResult listAll(@PathVariable Long videoId){
        List<TeacherImageResp> teacherImageRespList = teacherImageService.listAll(videoId);
        return success(teacherImageRespList);
    }

    @Log(title = "删除教师形象")
    @DeleteMapping("/delete/{id}")
    public AjaxResult deleteTeacherImage(@PathVariable Long id){
        teacherImageService.deleteTeacherImage(id);
        return success();
    }

}
