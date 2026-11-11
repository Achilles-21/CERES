package com.ceres.system.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvitationReplyDeletePolicyTest {

    @Test
    void allowsAuthorToDeleteOwnComment() {
        assertTrue(InvitationReplyDeletePolicy.canDelete(42L, 42L));
    }

    @Test
    void allowsAdminToDeleteAnyComment() {
        assertTrue(InvitationReplyDeletePolicy.canDelete(1L, 42L));
    }

    @Test
    void deniesOtherUsers() {
        assertFalse(InvitationReplyDeletePolicy.canDelete(7L, 42L));
    }
}
