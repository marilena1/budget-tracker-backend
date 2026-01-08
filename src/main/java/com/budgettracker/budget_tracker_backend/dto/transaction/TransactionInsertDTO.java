package com.budgettracker.budget_tracker_backend.dto.transaction;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a data transfer object for creating new financial transactions.
 * Contains transaction details including amount, date, and category reference.
 */
@Builder
public record TransactionInsertDTO(

        @NotBlank(message = "Category ID is required")
        String categoryId,

        @NotNull(message = "Amount is required")
        @Digits(integer = 10, fraction = 2, message = "Amount cannot have more than 2 decimal places")
        BigDecimal amount,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description,

        @NotNull(message = "Date is required")
        LocalDate date
) {}
