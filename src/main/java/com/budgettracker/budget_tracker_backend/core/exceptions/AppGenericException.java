package com.budgettracker.budget_tracker_backend.core.exceptions;

import lombok.Getter;

/**
 * Base class for all application-specific exceptions.
 * Provides a structured error response with a custom error code and message.
 * All custom exceptions in the application should extend this class.
 */
@Getter
public class AppGenericException extends Exception {

    private final String errorCode;

    /**
     * Constructs a new generic application exception.
     *
     * @param errorCode a machine-readable code identifying the error type
     * @param message a human-readable description of the error
     */
    public AppGenericException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
