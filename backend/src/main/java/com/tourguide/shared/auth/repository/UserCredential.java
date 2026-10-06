package com.tourguide.shared.auth.repository;

import com.tourguide.shared.model.User;

/** A user together with their stored bcrypt hash, used only for login verification. */
public record UserCredential(User user, String passwordHash) {
}
