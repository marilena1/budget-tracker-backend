package com.budgettracker.budget_tracker_backend.dto.transaction;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a data transfer object for reading financial transaction information.
 * Contains complete transaction data with denormalized category and user details.
 */
@Builder
public record TransactionReadOnlyDTO(
        String id,
        String userId,
        String userUsername,
        String categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal amount,
        String description,
        LocalDate date,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}