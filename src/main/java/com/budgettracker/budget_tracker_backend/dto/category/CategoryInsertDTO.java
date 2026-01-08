package com.budgettracker.budget_tracker_backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Represents a data transfer object for creating new transaction categories.
 * Contains user-provided data required to create a category entity.
 */
@Builder
public record CategoryInsertDTO (
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
        String name,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description,

        String color
) {}
