package com.tourguide.availability.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateAvailabilityRequest(
        @Min(1) Integer package_id,
        LocalDate start_date,
        LocalDate end_date,
        @Min(1) @Max(100) Integer max_group_size,
        @DecimalMin("0") BigDecimal price_override,
        @Pattern(regexp = "AVAILABLE|BLOCKED") String status,
        @Size(max = 300) String note
) {
}
