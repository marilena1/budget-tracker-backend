package com.budgettracker.budget_tracker_backend.core.exceptions;

/**
 * Thrown when a user attempts to perform an action they do not have permission for.
 * This is typically used for authorization checks after authentication has succeeded.
 * Example: A regular user trying to access an admin-only endpoint.
 */
public class AppObjectNotAuthorizedException extends AppGenericException {

    private static final String ERROR_CODE = "NOT_AUTHORIZED";

    /**
     * Creates an authorization exception for a specific action or resource.
     *
     * @param resourceDescription a description of the resource or action being attempted
     * @param requiredPermission the permission or role that was required but missing
     */
    public AppObjectNotAuthorizedException(String resourceDescription, String requiredPermission) {

        super(ERROR_CODE,
                String.format("Access to '%s' is denied. Required: %s.", resourceDescription, requiredPermission));
    }
}
