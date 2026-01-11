package com.budgettracker.budget_tracker_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Represents a data transfer object for authentication responses.
 * Contains user information and security token upon successful login.
 */
@Builder
public record AuthenticationResponseDTO(
        @NotBlank(message = "Username is required")
        String firstname,

        @NotBlank(message = "Username is required")
        String lastname,

        String token
) {}
