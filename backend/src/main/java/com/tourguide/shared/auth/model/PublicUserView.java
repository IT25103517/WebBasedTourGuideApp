package com.tourguide.shared.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourguide.shared.model.User;

import java.time.LocalDateTime;

/**
 * The restricted "user" object embedded in the register/login responses -
 * deliberately narrower than the full {@link User} hierarchy (no
 * is_active, no role-specific columns), matching what the API returned
 * before this refactor.
 */
public class PublicUserView {

    @JsonProperty("user_id")
    private final Integer userId;

    @JsonProperty("full_name")
    private final String fullName;

    private final String email;

    private final String phone;

    private final String role;

    @JsonProperty("date_created")
    private final LocalDateTime dateCreated;

    public PublicUserView(Integer userId, String fullName, String email, String phone,
                           String role, LocalDateTime dateCreated) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.dateCreated = dateCreated;
    }

    public static PublicUserView of(User user) {
        return new PublicUserView(user.getUserId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getRole(), user.getDateCreated());
    }

    public Integer getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
}
