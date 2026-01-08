package com.budgettracker.budget_tracker_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Represents a data transfer object for creating new user accounts.
 * Contains user-provided credentials and personal information for registration.
 */
@Builder
public record UserInsertDTO (

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$",
                message = "Password must contain at least 1 letter and 1 number")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        String firstname,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        String lastname
)
{}
