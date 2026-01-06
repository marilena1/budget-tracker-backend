package com.budgettracker.budget_tracker_backend.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a data transfer object for reading user account information.
 * Contains complete user profile data including roles and account status.
 */
@Builder
public record UserReadOnlyDTO (
        String id,
        String username,
        String email,
        String firstname,
        String lastname,
        boolean active,
        Set<String> roleNames,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
)
{}

