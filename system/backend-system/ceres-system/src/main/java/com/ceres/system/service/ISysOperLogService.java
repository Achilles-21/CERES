package com.ceres.system.service;

import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.SysOperLog;

import java.util.List;

public interface ISysOperLogService
{
    public void insertOperlog(SysOperLog operLog);

    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();

    PageResult pageQuery(Integer pageNum, Integer pageSize);

}
