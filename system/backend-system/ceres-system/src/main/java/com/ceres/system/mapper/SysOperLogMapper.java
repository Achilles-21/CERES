package com.ceres.system.mapper;

import com.ceres.system.domain.SysOperLog;
import com.github.pagehelper.Page;

import java.util.List;

public interface SysOperLogMapper
{
    public void insertOperlog(SysOperLog operLog);

    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();

    Page<SysOperLog> selectPage();

}
