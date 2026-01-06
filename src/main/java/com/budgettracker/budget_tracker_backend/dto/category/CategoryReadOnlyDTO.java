package com.budgettracker.budget_tracker_backend.dto.category;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Represents a data transfer object for reading category information.
 * Contains complete category data including system-generated metadata.
 */
@Builder
public record CategoryReadOnlyDTO(
        String id,
        String name,
        String description,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}