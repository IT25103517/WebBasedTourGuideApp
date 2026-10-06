package com.tourguide.itineraries.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A row from dbo.TourItineraries.
 * COMPOSITION: a day owns its {@link ItineraryActivity} list - an activity has
 * no meaning or lifecycle of its own outside the itinerary day it belongs to
 * (deleting the day deletes its activities).
 */
public class ItineraryDay {

    @JsonProperty("itinerary_id")
    private Integer itineraryId;

    @JsonProperty("package_id")
    private Integer packageId;

    @JsonProperty("day_number")
    private Integer dayNumber;

    @JsonProperty("day_title")
    private String dayTitle;

    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /** Only populated where the endpoint nests activities under a day. */
    private List<ItineraryActivity> activities;

    public ItineraryDay() {
    }

    public Integer getItineraryId() {
        return itineraryId;
    }

    public void setItineraryId(Integer itineraryId) {
        this.itineraryId = itineraryId;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDayTitle() {
        return dayTitle;
    }

    public void setDayTitle(String dayTitle) {
        this.dayTitle = dayTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ItineraryActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<ItineraryActivity> activities) {
        this.activities = activities;
    }
}
