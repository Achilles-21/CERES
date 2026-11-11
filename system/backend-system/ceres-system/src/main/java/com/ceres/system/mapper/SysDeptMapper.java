package com.ceres.system.mapper;

import com.ceres.common.core.domain.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper
{
    public List<SysDept> selectDeptList(SysDept dept);

    public List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    public SysDept selectDeptById(Long deptId);

    public List<SysDept> selectChildrenDeptById(Long deptId);

    public int selectNormalChildrenDeptById(Long deptId);

    public int hasChildByDeptId(Long deptId);

    public int checkDeptExistUser(Long deptId);

    public SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);

    public int insertDept(SysDept dept);

    public int updateDept(SysDept dept);

    public void updateDeptStatusNormal(Long[] deptIds);

    public int updateDeptChildren(@Param("depts") List<SysDept> depts);

    public int deleteDeptById(Long deptId);
}
