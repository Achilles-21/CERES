package com.ceres.system.service;

import com.ceres.system.domain.SysPost;

import java.util.List;

public interface ISysPostService
{
    public List<SysPost> selectPostList(SysPost post);

    public List<SysPost> selectPostAll();

    public SysPost selectPostById(Long postId);

    public List<Long> selectPostListByUserId(Long userId);

    public boolean checkPostNameUnique(SysPost post);

    public boolean checkPostCodeUnique(SysPost post);

    public int countUserPostById(Long postId);

    public int deletePostById(Long postId);

    public int deletePostByIds(Long[] postIds);

    public int insertPost(SysPost post);

    public int updatePost(SysPost post);
}
