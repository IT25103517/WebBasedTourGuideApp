package com.tourguide.offers.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateOfferRequest(
        @NotBlank @Size(min = 3, max = 150) String title,
        @Size(max = 1000) String description,
        @NotBlank @Pattern(regexp = "PERCENT|FIXED") String discount_type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal discount_value,
        @NotNull LocalDate start_date,
        @NotNull LocalDate end_date,
        @Pattern(regexp = "ACTIVE|INACTIVE|EXPIRED") String status,
        List<Integer> package_ids
) {
}
