package com.ceres.system.mapper;

import com.ceres.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface SysUserRoleMapper
{
    public int deleteUserRoleByUserId(@Param("userId") Long userId);

    public int deleteUserRole(Long[] ids);

    public int countUserRoleByRoleId(Long roleId);

    public int batchUserRole(List<SysUserRole> userRoleList);

    public int deleteUserRoleInfo(SysUserRole userRole);

    public int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);

    void insert(@Param("userRole") SysUserRole userRole);

    List<SysUserRole> getUserByIds(@Param("userIds") Set<Long> userIds);

    SysUserRole selectUserRoleById(@Param("userId") Long userId);

}
