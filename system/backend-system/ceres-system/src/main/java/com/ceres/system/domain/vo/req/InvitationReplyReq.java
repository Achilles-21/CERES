package com.ceres.system.domain.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class InvitationReplyReq {

    private Long invitationId;

    private String replyContent;

    /** Parent reply ID; null indicates a reply directly under the post. */
    private Long invitationReplyId;

    /** Target user ID for reply display; null indicates a reply directly under the post. */
    private Long targetUserId;

    /** Target comment ID; null indicates a comment directly under the post or its reply. */
    private Long targetCommentId;

    private List<String> replyPictureList;
}
