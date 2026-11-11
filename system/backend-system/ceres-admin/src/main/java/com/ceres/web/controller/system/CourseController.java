package com.ceres.web.controller.system;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.domain.Que;
import com.ceres.system.domain.VideoPop;
import com.ceres.system.domain.vo.resp.PlayVideoResp;
import com.ceres.system.domain.vo.resp.ResourceVideoResp;
import com.ceres.system.domain.vo.resp.TeacherImageResp;
import com.ceres.system.domain.vo.resp.VideoPopResp;
import com.ceres.system.mapper.FileInfoMapper;
import com.ceres.system.mapper.QuePopRecordMapper;
import com.ceres.system.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("course")
public class CourseController {

    @Resource
    private IResourceVideoService resourceVideoService;

    @Resource
    private ICourseRecordService courseRecordService;

    @Resource
    private ITeacherImageService teacherImageService;

    @Resource
    private IVideoPopQueService videoPopQueService;

    @Resource
    private IQueService queService;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private QuePopRecordMapper quePopRecordMapper;

    @Log(title = "根据id获取课程信息")
    @GetMapping("play")
    public AjaxResult playVideo(@RequestParam("id") Long videoId) {
        ResourceVideoResp video = resourceVideoService.getVideoById(videoId);
        Double playingTime = courseRecordService.playVideo(videoId);

        List<TeacherImageResp> teacherImageRespList = teacherImageService.listAll(videoId);
        PlayVideoResp resp = new PlayVideoResp();
        BeanUtils.copyProperties(video, resp);
        resp.setPlayingTime(playingTime);
        resp.setTeacherImageRespList(teacherImageRespList);

        LambdaQueryWrapper<VideoPop> lambdaQueryWrapper = new LambdaQueryWrapper<VideoPop>().eq(VideoPop::getVideoId, videoId);
        List<VideoPop> videoPopList = videoPopQueService.list(lambdaQueryWrapper);
        List<VideoPopResp> videoPopRespList = BeanUtil.copyToList(videoPopList, VideoPopResp.class);
        List<VideoPopResp> videoPopRespListFinal = new ArrayList<VideoPopResp>();

        for(VideoPopResp videoPopResp : videoPopRespList) {
            Boolean isAnswered = videoPopQueService.getIsAnswered(videoPopResp.getId());
            if(!isAnswered) {
                List<String> queIdStr = Arrays.asList(videoPopResp.getQueId().split(" "));
                List<Long> queIdInt = queIdStr.stream().map(Long::parseLong).collect(Collectors.toList());
                List<Que> queList = queService.selectBatchIds(queIdInt);
                videoPopResp.setQueList(queList);
                videoPopRespListFinal.add(videoPopResp);
            }
        }

        resp.setVideoPopRespList(videoPopRespListFinal);
        return AjaxResult.success(resp);
    }

    @Log(title = "更新播放时间")
    @GetMapping("updatePlayingTime")
    public AjaxResult updatePlayingTime(@RequestParam("id") Long videoId, @RequestParam("playingTime") Double playingTime) {
        courseRecordService.updateCoursePlayTime(videoId, playingTime);
        return AjaxResult.success();
    }

    @Log(title = "获取记录列表")
    @GetMapping("getRecordList")
    public AjaxResult getRecordList(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize) {
        return AjaxResult.success(courseRecordService.getCourseRecordList(pageNum, pageSize));
    }

    @Log(title = "获取课程列表")
    @GetMapping("getCourseList")
    public AjaxResult getCourseList(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize) {
        return AjaxResult.success(courseRecordService.getCourseList(pageNum, pageSize));
    }

    @Log(title = "用户点击视频")
    @GetMapping("/hits")
    public AjaxResult hits(@RequestParam("id") Long id){
        courseRecordService.hits(id);
        return AjaxResult.success();
    }

}
