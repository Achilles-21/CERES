package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.service.ISysUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/teacher/picture")
public class TeacherPictureController {

    @Resource
    private ISysUserService sysUserService;

    @Log(title = "修改教师形象")
    @PostMapping("/update")
    public AjaxResult updateTeacherPicture(@RequestBody Map<String, Integer> map){
        Integer teacherPicture = map.get("teacherPicture");
        sysUserService.updateTeacherPicture(teacherPicture);
        return AjaxResult.success();
    }

}
