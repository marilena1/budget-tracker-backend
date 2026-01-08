package com.budgettracker.budget_tracker_backend.core.exceptions;

/**
 * Thrown when attempting to create an entity that already exists in the system.
 * Example: duplicate username, email, or category name.
 */
public class AppObjectAlreadyExistsException extends AppGenericException  {

    private static final String ERROR_CODE = "OBJECT_ALREADY_EXISTS";

    /**
     * Constructs a new exception for a duplicate entity.
     *
     * @param entityName the type of object (e.g., "User", "Category")
     * @param identifier the duplicate value (e.g., the username, email, or name)
     */
    public AppObjectAlreadyExistsException(String entityName, String identifier) {
        super(ERROR_CODE,
                String.format("%s with identifier '%s' already exists.", entityName, identifier));
    }
}
