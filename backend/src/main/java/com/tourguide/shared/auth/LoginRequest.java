package com.tourguide.shared.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "email is required")
        @Size(max = 160)
        String email,

        @NotBlank(message = "password is required")
        @Size(max = 100)
        String password
) {
}
