package com.ceres.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.system.domain.TeacherImage;
import com.ceres.system.domain.vo.req.TeacherImageReq;
import com.ceres.system.domain.vo.resp.TeacherImageResp;
import com.ceres.system.mapper.TeacherImageMapper;
import com.ceres.system.service.ITeacherImageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherImageServiceImpl extends ServiceImpl<TeacherImageMapper, TeacherImage> implements ITeacherImageService {

    @Override
    public void addTeacherImage(TeacherImageReq teacherImageReq) {
        TeacherImage teacherImage = BeanUtil.toBean(teacherImageReq, TeacherImage.class);
        teacherImage.setCreateTime(LocalDateTime.now());
        teacherImage.setCreateBy(SecurityUtils.getUsername());
        baseMapper.insert(teacherImage);
    }

    @Override
    public void updateTeacherImage(TeacherImageReq teacherImageReq) {
        TeacherImage teacherImage = BeanUtil.toBean(teacherImageReq, TeacherImage.class);
        baseMapper.updateById(teacherImage);
    }

    @Override
    public TeacherImageResp getTeacherImageById(Long id) {
        TeacherImage teacherImage = baseMapper.selectById(id);
        return BeanUtil.toBean(teacherImage, TeacherImageResp.class);
    }

    @Override
    public List<TeacherImageResp> listAll(Long videoId) {
        LambdaQueryWrapper<TeacherImage> eq = new LambdaQueryWrapper<TeacherImage>().eq(TeacherImage::getVideoId, videoId).orderByDesc(TeacherImage::getImageTime);
        List<TeacherImage> teacherImageList = baseMapper.selectList(eq);
        List<TeacherImageResp> teacherImageRespList = BeanUtil.copyToList(teacherImageList, TeacherImageResp.class);
        return teacherImageRespList;
    }

    @Override
    public void deleteTeacherImage(Long id) {
        baseMapper.deleteById(id);
    }

}
