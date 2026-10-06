package com.tourguide.shared.security;

import com.tourguide.shared.error.ApiException;

import java.util.Arrays;

/**
 * The logged in user, read from the JWT (req.user in the Node middleware).
 * id is the user_id / tourist_id / guide_id / admin_id (all the same PK
 * value, per the ISA hierarchy in the schema).
 */
public record CurrentUserDetails(Integer id, String role, String name) {

    /** Port of middleware/auth.js's authorize(...roles). */
    public void requireRole(String... roles) {
        if (!Arrays.asList(roles).contains(role)) {
            throw ApiException.forbidden("This action requires role: " + String.join(" or ", roles));
        }
    }
}
