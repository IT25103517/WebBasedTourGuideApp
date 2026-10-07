package com.tourguide.itineraries.dto;

import jakarta.validation.constraints.*;

public record CreateDayRequest(
        @NotNull @Min(1) Integer package_id,
        @NotNull @Min(1) @Max(60) Integer day_number,
        @NotBlank @Size(min = 3, max = 150) String day_title,
        @Size(max = 1500) String description
) {
}
