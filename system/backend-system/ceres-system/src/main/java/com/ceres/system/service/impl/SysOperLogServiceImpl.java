package com.ceres.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.SysOperLog;
import com.ceres.system.mapper.SysOperLogMapper;
import com.ceres.system.service.ISysOperLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysOperLogServiceImpl implements ISysOperLogService
{
    @Autowired
    private SysOperLogMapper operLogMapper;

    @Override
    public void insertOperlog(SysOperLog operLog)
    {
        operLogMapper.insertOperlog(operLog);
    }

    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog)
    {
        return operLogMapper.selectOperLogList(operLog);
    }

    @Override
    public int deleteOperLogByIds(Long[] operIds)
    {
        return operLogMapper.deleteOperLogByIds(operIds);
    }

    @Override
    public SysOperLog selectOperLogById(Long operId)
    {
        return operLogMapper.selectOperLogById(operId);
    }

    @Override
    public void cleanOperLog()
    {
        operLogMapper.cleanOperLog();
    }

    @Override
    public PageResult pageQuery(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<SysOperLog> sysOperLogPage = operLogMapper.selectPage();
        if(CollectionUtil.isEmpty(sysOperLogPage.getResult())){
            return new PageResult(ListUtil.empty(), 0, pageNum, pageSize);
        }
        return new PageResult(sysOperLogPage.getResult(), sysOperLogPage.getTotal(), sysOperLogPage.getPageNum(), sysOperLogPage.getPageSize());
    }

}
