package com.hackathon.payment.security;

import com.hackathon.payment.user.UserRole;

import java.util.UUID;

/**
 * Principal placed into the SecurityContext once a JWT is validated.
 */
public record AuthenticatedUser(UUID id, String username, UserRole role) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
