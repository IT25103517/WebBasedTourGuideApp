package com.tourguide.shared.model;

import java.time.LocalDateTime;

/** A user whose role is ADMIN. dbo.Administrators adds no extra columns beyond the PK - inherits every field/method from {@link User}. */
public class Administrator extends User {

    public Administrator() {
        super();
    }

    public Administrator(Integer userId, String fullName, String email, String phone,
                          String role, Boolean isActive, LocalDateTime dateCreated) {
        super(userId, fullName, email, phone, role, isActive, dateCreated);
    }

    @Override
    public String roleLabel() {
        return "Administrator";
    }
}
