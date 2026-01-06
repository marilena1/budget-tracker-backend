package com.budgettracker.budget_tracker_backend.dto.common;

/**
 * Standardized response message for API operations.
 * Used for simple success/error responses where no data needs to be returned.
 */
public record ResponseMessageDTO(String code, String description) {
    /**
     * Creates a response with only a code (empty description).
     * Useful for simple success responses.
     *
     * @param code the response code (e.g., "SUCCESS", "USER_CREATED")
     */
    public ResponseMessageDTO(String code) {
        this(code, "");
    }
}
