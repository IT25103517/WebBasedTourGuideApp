package com.tourguide.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Base type for every account row in dbo.Users. Concrete subtypes
 * (Tourist / TourGuide / Administrator) add the columns from their own
 * ISA table, mirroring the schema's single-table-per-role inheritance.
 *
 * INHERITANCE + ABSTRACTION: User is declared abstract on purpose - it is
 * never instantiated on its own, only through a concrete subtype. It factors
 * out the fields and behaviour every account shares (name, email, role, ...)
 * so Tourist/TourGuide/Administrator only need to add what makes them different.
 *
 * ENCAPSULATION: all fields are private with public getters/setters, so
 * outside code can only read or change state through those methods.
 *
 * {@link #roleLabel()} is the polymorphic hook each subtype overrides so
 * callers can render a human label without switching on role.
 */
public abstract class User {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    private String phone;

    private String role;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    protected User() {
    }

    protected User(Integer userId, String fullName, String email, String phone,
                    String role, Boolean isActive, LocalDateTime dateCreated) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
        this.dateCreated = dateCreated;
    }

    /**
     * A human-readable label for this user's role, e.g. for a /me response or a log line.
     * POLYMORPHISM: calling user.roleLabel() runs a different method body depending on
     * whether user is actually a Tourist, TourGuide or Administrator at runtime - the
     * caller never needs an if/switch on role to know which.
     */
    public abstract String roleLabel();

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
