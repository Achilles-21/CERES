package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.CourseRecord;

public interface ICourseRecordService {

    CourseRecord getCourseRecordById(Long videoId);

    void updateCoursePlayTime(Long videoId, Double playingTime);

    Double playVideo(Long videoId);

    PageResult getCourseRecordList(Integer pageNum, Integer pageSize);

    PageResult getCourseList(Integer pageNum, Integer pageSize);

    void hits(Long id);

}
