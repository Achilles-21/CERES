package com.ceres.system.service;

import com.ceres.system.domain.SysLogininfor;

import java.util.List;

public interface ISysLogininforService
{
    public void insertLogininfor(SysLogininfor logininfor);

    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public void cleanLogininfor();
}
