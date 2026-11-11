package com.ceres.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.Invitation;
import com.ceres.system.domain.vo.req.InvitationLikeReq;
import com.ceres.system.domain.vo.req.InvitationReq;
import com.ceres.system.domain.vo.resp.InvitationResp;

import java.util.List;
import java.util.Set;

public interface IInvitationService extends IService<Invitation> {

    PageResult pageQuery(Integer pageNum, Integer pageSize);

    void add(InvitationReq invitationReq);

    void delete(Long id);

    InvitationResp selectById(Long id);

    void likes(InvitationLikeReq likeReq);

    Set<Long> likedList(List<Long> invitationIds);

}
