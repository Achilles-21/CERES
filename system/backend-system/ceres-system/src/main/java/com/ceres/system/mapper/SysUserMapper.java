package com.ceres.system.mapper;

import com.ceres.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface SysUserMapper
{
    public List<SysUser> selectUserList(SysUser sysUser);

    public List<SysUser> selectAllocatedList(SysUser user);

    public List<SysUser> selectUnallocatedList(SysUser user);

    public SysUser selectUserByUsername(String username);

    public SysUser selectUserById(Long userId);

    public int insertUser(SysUser user);

    public int updateUser(SysUser user);

    public int updateUserAvatar(@Param("username") String username, @Param("avatar") String avatar);

    public int resetUserPwd(@Param("username") String username, @Param("password") String password);

    public int deleteUserById(Long userId);

    public int deleteUserByIds(Long[] userIds);

    public SysUser checkUsernameUnique(String username);

    public SysUser checkPhoneUnique(String phonenumber);

    public SysUser checkEmailUnique(String email);

    void editUserAvatar(@Param("userId") Long userId, @Param("imageUrl") String imageUrl);

    List<SysUser> selectUserByIds(@Param("userIds") List<Long> userIds);

    void updateTeacherPicture(@Param("teacherPicture") Integer teacherPicture, @Param("userId") Long userId);

    List<SysUser> getUserByIds(@Param("userIds") Set<Long> userIds);

    SysUser getUserById(@Param("userId") Long userId);

}
