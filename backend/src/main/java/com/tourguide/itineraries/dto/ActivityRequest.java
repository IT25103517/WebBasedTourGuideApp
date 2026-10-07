package com.tourguide.itineraries.dto;

import jakarta.validation.constraints.*;

public record ActivityRequest(
        @NotBlank @Size(min = 2, max = 150) String activity_name,
        @Size(max = 150) String location,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "start_time must be HH:MM (24 hour)") String start_time,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "end_time must be HH:MM (24 hour)") String end_time,
        @Min(1) @Max(100) Integer sort_order
) {
}
