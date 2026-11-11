package com.ceres.system.mapper;

import com.ceres.system.domain.SysRoleDept;

import java.util.List;

public interface SysRoleDeptMapper
{
    public int deleteRoleDeptByRoleId(Long roleId);

    public int deleteRoleDept(Long[] ids);

    public int selectCountRoleDeptByDeptId(Long deptId);

    public int batchRoleDept(List<SysRoleDept> roleDeptList);
}
