package com.ceres.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ceres.system.domain.TeacherImage;
import com.ceres.system.domain.vo.req.TeacherImageReq;
import com.ceres.system.domain.vo.resp.TeacherImageResp;

import java.util.List;

public interface ITeacherImageService extends IService<TeacherImage> {

    void addTeacherImage(TeacherImageReq teacherImageReq);

    void updateTeacherImage(TeacherImageReq teacherImageReq);

    TeacherImageResp getTeacherImageById(Long id);

    List<TeacherImageResp> listAll(Long videoId);

    void deleteTeacherImage(Long id);

}
