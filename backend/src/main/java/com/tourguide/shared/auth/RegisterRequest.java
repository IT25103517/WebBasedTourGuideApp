package com.tourguide.shared.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Port of the register() validation rules in shared/auth.routes.js. */
public record RegisterRequest(

        @NotBlank(message = "full_name is required")
        @Size(min = 3, max = 120, message = "full_name must be between 3 and 120 characters")
        String full_name,

        @NotBlank(message = "email is required")
        @Size(max = 160, message = "email must be at most 160 characters")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "email must be a valid address")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password,

        @Size(max = 20, message = "phone must be at most 20 characters")
        String phone,

        @NotBlank(message = "role is required")
        @Pattern(regexp = "TOURIST|GUIDE|ADMIN", message = "role must be one of: TOURIST, GUIDE, ADMIN")
        String role,

        @Size(max = 60)
        String nationality,

        @Size(max = 60)
        String preferred_language,

        @Min(0) @Max(70)
        Integer experience_years,

        @Size(max = 200)
        String languages,

        @Size(max = 100)
        String base_location
) {
}
