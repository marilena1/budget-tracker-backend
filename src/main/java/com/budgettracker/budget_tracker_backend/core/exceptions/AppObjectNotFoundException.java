package com.budgettracker.budget_tracker_backend.core.exceptions;

/**
 * Thrown when a requested entity or resource cannot be found in the system.
 * This typically corresponds to an HTTP 404 Not Found status.
 * Example: A user, transaction, or category referenced by an ID does not exist.
 */
public class AppObjectNotFoundException extends AppGenericException {

    private static final String ERROR_CODE = "NOT_FOUND";

    /**
     * Creates a "not found" exception for a specific entity.
     *
     * @param entityName the type of object that was not found (e.g., "User", "Transaction")
     * @param identifier the unique identifier that was searched for (e.g., an ID, username, or name)
     */
    public AppObjectNotFoundException(String entityName, String identifier) {

        super(ERROR_CODE,
                String.format("%s with identifier '%s' was not found.", entityName, identifier));
    }
}
