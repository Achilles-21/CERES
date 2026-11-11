package com.ceres.system.mapper;

import com.ceres.system.domain.SysLogininfor;

import java.util.List;

public interface SysLogininforMapper
{
    public void insertLogininfor(SysLogininfor logininfor);

    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public int cleanLogininfor();
}
