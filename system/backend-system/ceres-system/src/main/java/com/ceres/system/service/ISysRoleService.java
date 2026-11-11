package com.ceres.system.service;

import com.ceres.common.core.domain.entity.SysRole;
import com.ceres.system.domain.SysUserRole;

import java.util.List;
import java.util.Set;

public interface ISysRoleService
{
    public List<SysRole> selectRoleList(SysRole role);

    public List<SysRole> selectRolesByUserId(Long userId);

    public Set<String> selectRolePermissionByUserId(Long userId);

    public List<SysRole> selectRoleAll();

    public List<Long> selectRoleListByUserId(Long userId);

    public SysRole selectRoleById(Long roleId);

    public boolean checkRoleNameUnique(SysRole role);

    public boolean checkRoleKeyUnique(SysRole role);

    public void checkRoleAllowed(SysRole role);

    public void checkRoleDataScope(Long... roleIds);

    public int countUserRoleByRoleId(Long roleId);

    public int insertRole(SysRole role);

    public int updateRole(SysRole role);

    public int updateRoleStatus(SysRole role);

    public int authDataScope(SysRole role);

    public int deleteRoleById(Long roleId);

    public int deleteRoleByIds(Long[] roleIds);

    public int deleteAuthUser(SysUserRole userRole);

    public int deleteAuthUsers(Long roleId, Long[] userIds);

    public int insertAuthUsers(Long roleId, Long[] userIds);

    public void insertUserRole(SysUserRole userRole);

}
