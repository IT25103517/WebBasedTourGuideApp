package com.tourguide.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** A user whose role is GUIDE, adding the columns from dbo.TourGuides - inherits every base field/method from {@link User}. */
public class TourGuide extends User {

    @JsonProperty("experience_years")
    private Integer experienceYears;

    @JsonProperty("verification_status")
    private String verificationStatus;

    private String bio;

    private String languages;

    @JsonProperty("base_location")
    private String baseLocation;

    public TourGuide() {
        super();
    }

    public TourGuide(Integer userId, String fullName, String email, String phone,
                      String role, Boolean isActive, LocalDateTime dateCreated,
                      Integer experienceYears, String verificationStatus, String bio,
                      String languages, String baseLocation) {
        super(userId, fullName, email, phone, role, isActive, dateCreated);
        this.experienceYears = experienceYears;
        this.verificationStatus = verificationStatus;
        this.bio = bio;
        this.languages = languages;
        this.baseLocation = baseLocation;
    }

    @Override
    public String roleLabel() {
        return "Tour Guide";
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }
}
