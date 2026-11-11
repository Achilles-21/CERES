package com.ceres.system.service;

import com.ceres.common.core.domain.TreeSelect;
import com.ceres.common.core.domain.entity.SysDept;

import java.util.List;

public interface ISysDeptService
{
    public List<SysDept> selectDeptList(SysDept dept);

    public List<TreeSelect> selectDeptTreeList(SysDept dept);

    public List<SysDept> buildDeptTree(List<SysDept> depts);

    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

    public List<Long> selectDeptListByRoleId(Long roleId);

    public SysDept selectDeptById(Long deptId);

    public int selectNormalChildrenDeptById(Long deptId);

    public boolean hasChildByDeptId(Long deptId);

    public boolean checkDeptExistUser(Long deptId);

    public boolean checkDeptNameUnique(SysDept dept);

    public void checkDeptDataScope(Long deptId);

    public int insertDept(SysDept dept);

    public int updateDept(SysDept dept);

    public int deleteDeptById(Long deptId);
}
