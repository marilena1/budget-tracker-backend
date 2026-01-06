package com.budgettracker.budget_tracker_backend.dto.user;

import lombok.Builder;

/**
 * Represents a data transfer object for creating new user accounts.
 * Contains user-provided credentials and personal information for registration.
 */
@Builder
public record UserInsertDTO (
        String username,
        String password,
        String email,
        String firstname,
        String lastname
)
{}
