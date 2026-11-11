package com.ceres.system.mapper;

import com.ceres.system.domain.SysRoleMenu;

import java.util.List;

public interface SysRoleMenuMapper
{
    public int checkMenuExistRole(Long menuId);

    public int deleteRoleMenuByRoleId(Long roleId);

    public int deleteRoleMenu(Long[] ids);

    public int batchRoleMenu(List<SysRoleMenu> roleMenuList);
}
