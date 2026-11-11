package com.ceres.system.domain.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class InvitationReplyCommentResp {

    private Long id;

    private Long invitationId;

    /** Parent reply ID; null indicates a reply directly under the post. */
    private Long invitationReplyId;

    private Long userId;

    private String nickName;

    private String avatar;

    private String replyContent;

    private Integer numberComments;

    private List<String> replyPictureList;

    private List<InvitationReplyCommentResp> replyCommentRespList;

    /** Target user ID for reply display; null indicates a reply directly under the post. */
    private Long targetUserId;

    /** Target nickname for reply display; null indicates a reply directly under the post. */
    private String targetUserName;

    /** Target comment ID; null indicates a comment directly under the post or its reply. */
    private Long targetCommentId;

}
