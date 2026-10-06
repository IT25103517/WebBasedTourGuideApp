package com.tourguide.shared.auth.model;

/** The {user, token} payload returned by /api/auth/register and /login. */
public class AuthResult {

    private final PublicUserView user;
    private final String token;

    public AuthResult(PublicUserView user, String token) {
        this.user = user;
        this.token = token;
    }

    public PublicUserView getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
