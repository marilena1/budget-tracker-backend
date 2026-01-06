package com.budgettracker.budget_tracker_backend.dto.auth;

import lombok.Builder;

/**
 * Represents a data transfer object for authentication responses.
 * Contains user information and security token upon successful login.
 */
@Builder
public record AuthenticationResponseDTO(String firstname, String lastname, String token) {}
