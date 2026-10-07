package com.tourguide.itineraries.dto;

import jakarta.validation.constraints.*;

public record UpdateDayRequest(
        @Min(1) @Max(60) Integer day_number,
        @Size(min = 3, max = 150) String day_title,
        @Size(max = 1500) String description
) {
}
