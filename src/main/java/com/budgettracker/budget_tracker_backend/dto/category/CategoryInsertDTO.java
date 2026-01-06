package com.budgettracker.budget_tracker_backend.dto.category;

import lombok.Builder;

/**
 * Represents a data transfer object for creating new transaction categories.
 * Contains user-provided data required to create a category entity.
 */
@Builder
public record CategoryInsertDTO (
    String name,
    String description,
    String color
) {}
