package com.budgettracker.budget_tracker_backend.core.exceptions;

/**
 * Thrown when a method receives an argument that is invalid or violates business rules.
 * Example: empty required field, malformed email format.
 */
public class AppObjectInvalidArgumentException extends AppGenericException {

    private static final String ERROR_CODE = "INVALID_ARGUMENT";

    /**
     * Creates an exception for an invalid argument.
     * @param fieldName The name of the invalid field/argument
     * @param requirement The validation rule that was broken
     */
    public AppObjectInvalidArgumentException(String fieldName, String requirement) {
        super(ERROR_CODE,
                String.format("Field '%s' is invalid: %s", fieldName, requirement));
    }
}
