package com.ceres.system.mapper;

import com.ceres.system.domain.SysPost;

import java.util.List;

public interface SysPostMapper
{
    public List<SysPost> selectPostList(SysPost post);

    public List<SysPost> selectPostAll();

    public SysPost selectPostById(Long postId);

    public List<Long> selectPostListByUserId(Long userId);

    public List<SysPost> selectPostsByUsername(String username);

    public int deletePostById(Long postId);

    public int deletePostByIds(Long[] postIds);

    public int updatePost(SysPost post);

    public int insertPost(SysPost post);

    public SysPost checkPostNameUnique(String postName);

    public SysPost checkPostCodeUnique(String postCode);
}
