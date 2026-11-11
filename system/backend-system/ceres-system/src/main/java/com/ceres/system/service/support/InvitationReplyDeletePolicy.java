package com.ceres.system.service.support;

import com.ceres.common.utils.SecurityUtils;

public final class InvitationReplyDeletePolicy {

    private InvitationReplyDeletePolicy() {
    }

    public static boolean canDelete(Long currentUserId, Long replyOwnerUserId) {
        return SecurityUtils.isAdmin(currentUserId) || (currentUserId != null && currentUserId.equals(replyOwnerUserId));
    }
}
