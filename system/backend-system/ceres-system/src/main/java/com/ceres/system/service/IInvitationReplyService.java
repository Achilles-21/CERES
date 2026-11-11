package com.ceres.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.InvitationReply;
import com.ceres.system.domain.vo.req.InvitationReplyReq;
import com.ceres.system.domain.vo.resp.InvitationReplyResp;

public interface IInvitationReplyService extends IService<InvitationReply> {

    InvitationReplyResp add(InvitationReplyReq replyReq);

    PageResult pageQuery(Integer pageNum, Integer pageSize, Long invitationId);

    PageResult listComment(Integer pageNum, Integer pageSize, Long invitationReplyId);

    void delete(Long id);

}
