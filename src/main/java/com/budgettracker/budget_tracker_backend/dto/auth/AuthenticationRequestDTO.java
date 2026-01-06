package com.budgettracker.budget_tracker_backend.dto.auth;

import lombok.Builder;

/**
 * Represents a data transfer object for authentication requests.
 * Contains user credentials for login verification.
 */
@Builder
public record AuthenticationRequestDTO(String username, String password) {}