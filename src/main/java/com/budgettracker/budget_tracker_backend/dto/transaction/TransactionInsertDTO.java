package com.budgettracker.budget_tracker_backend.dto.transaction;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a data transfer object for creating new financial transactions.
 * Contains transaction details including amount, date, and category reference.
 */
@Builder
public record TransactionInsertDTO(
        String categoryId,
        BigDecimal amount,
        String description,
        LocalDate date
) {}
