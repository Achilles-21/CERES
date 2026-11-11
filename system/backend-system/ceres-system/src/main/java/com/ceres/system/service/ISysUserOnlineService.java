package com.ceres.system.service;

import com.ceres.common.core.domain.model.LoginUser;
import com.ceres.system.domain.SysUserOnline;

public interface ISysUserOnlineService
{
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user);

    public SysUserOnline selectOnlineByUsername(String username, LoginUser user);

    public SysUserOnline selectOnlineByInfo(String ipaddr, String username, LoginUser user);

    public SysUserOnline loginUserToUserOnline(LoginUser user);
}
