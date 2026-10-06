package com.tourguide.itineraries.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A row from dbo.ItineraryActivities. start_time/end_time are kept as
 * plain "HH:mm" strings (not java.time.LocalTime) because that is exactly
 * the 5-character shape the original Map-based service produced via its
 * toHHMM() helper, and the frontend expects that shape verbatim.
 */
public class ItineraryActivity {

    @JsonProperty("activity_id")
    private Integer activityId;

    @JsonProperty("itinerary_id")
    private Integer itineraryId;

    @JsonProperty("activity_name")
    private String activityName;

    private String location;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    public ItineraryActivity() {
    }

    public ItineraryActivity(Integer activityId, Integer itineraryId, String activityName, String location,
                              String startTime, String endTime, Integer sortOrder) {
        this.activityId = activityId;
        this.itineraryId = itineraryId;
        this.activityName = activityName;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sortOrder = sortOrder;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getItineraryId() {
        return itineraryId;
    }

    public void setItineraryId(Integer itineraryId) {
        this.itineraryId = itineraryId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
