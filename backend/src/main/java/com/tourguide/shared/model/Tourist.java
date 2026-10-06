package com.tourguide.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** A user whose role is TOURIST, adding the columns from dbo.Tourists - inherits every base field/method from {@link User}. */
public class Tourist extends User {

    private String nationality;

    @JsonProperty("preferred_language")
    private String preferredLanguage;

    public Tourist() {
        super();
    }

    public Tourist(Integer userId, String fullName, String email, String phone,
                    String role, Boolean isActive, LocalDateTime dateCreated,
                    String nationality, String preferredLanguage) {
        super(userId, fullName, email, phone, role, isActive, dateCreated);
        this.nationality = nationality;
        this.preferredLanguage = preferredLanguage;
    }

    @Override
    public String roleLabel() {
        return "Tourist";
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
