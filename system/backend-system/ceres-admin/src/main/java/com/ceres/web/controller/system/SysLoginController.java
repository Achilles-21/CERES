package com.ceres.web.controller.system;

import com.ceres.common.constant.Constants;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.domain.entity.SysMenu;
import com.ceres.common.core.domain.entity.SysUser;
import com.ceres.common.core.domain.model.LoginBody;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.framework.web.service.SysLoginService;
import com.ceres.framework.web.service.SysPermissionService;
import com.ceres.system.service.ISysMenuService;
import com.ceres.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService sysUserService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid(), loginBody.getType());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        Long userId = SecurityUtils.getUserId();
        SysUser sysUser = sysUserService.selectUserById(userId);
        SysUser user = SecurityUtils.getLoginUser().getUser();
        BeanUtils.copyProperties(sysUser, user);
        user.setAvatar(sysUser.getAvatar());
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
