package com.budgettracker.budget_tracker_backend.core.exceptions;

import lombok.Getter;

/**
 * Represents an unexpected internal server error that is not a business rule failure.
 * This exception should wrap lower-level exceptions (e.g., from databases or external services)
 * to provide a consistent error structure. Typically results in an HTTP 500 Internal Server Error.
 */
@Getter
public class AppServerException extends AppGenericException {

    /**
     * Constructs a new internal server error exception.
     *
     * @param errorCode a machine-readable code for the internal error
     * @param message a human-readable description of the error
     */
    public AppServerException(String errorCode, String message) {
        super(errorCode, message);
    }
}
