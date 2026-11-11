package com.ceres.system.mapper;

import com.ceres.common.core.domain.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface SysRoleMapper
{
    public List<SysRole> selectRoleList(SysRole role);

    public List<SysRole> selectRolePermissionByUserId(Long userId);

    public List<SysRole> selectRoleAll();

    public List<Long> selectRoleListByUserId(Long userId);

    public SysRole selectRoleById(Long roleId);

    public List<SysRole> selectRolesByUsername(String username);

    public SysRole checkRoleNameUnique(String roleName);

    public SysRole checkRoleKeyUnique(String roleKey);

    public int updateRole(SysRole role);

    public int insertRole(SysRole role);

    public int deleteRoleById(Long roleId);

    public int deleteRoleByIds(Long[] roleIds);

    List<SysRole> selectRoleByIds(@Param("roleIds") Set<Long> roleIds);

    SysRole selectById(Long roleId);

}
