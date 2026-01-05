package com.budgettracker.budget_tracker_backend.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * Carries Spring's validation results from @Valid failures.
 * Used when request data fails annotation-based validation (e.g., @NotBlank, @Email).
 * This is a Spring infrastructure exception, not a business logic exception.
 */
@Getter
public class ValidationException extends Exception {

    private final BindingResult bindingResult;

    /**
     * Constructs a new validation exception with Spring's detailed binding results.
     * This exception captures all field-level errors from a failed {@code @Valid} request.
     *
     * @param bindingResult the Spring BindingResult containing all validation errors
     *                      for individual fields (e.g., "email must be valid", "name cannot be empty")
     */
    public ValidationException(BindingResult bindingResult) {
        super("Validation failed for the submitted request");
        this.bindingResult = bindingResult;
    }
}
