package com.tourguide.itineraries.dto;

import java.util.List;

/** Body of PUT /api/itineraries/package/{packageId} - a full bulk replace. */
public record ReplaceItineraryRequest(List<DayPayload> days) {

    public record DayPayload(Integer day_number, String day_title, String description,
                              List<ActivityPayload> activities) {
    }

    public record ActivityPayload(String activity_name, String location,
                                   String start_time, String end_time, Integer sort_order) {
    }
}
