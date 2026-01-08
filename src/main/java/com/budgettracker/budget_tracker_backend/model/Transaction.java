package com.budgettracker.budget_tracker_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a financial transaction in the system.
 * Transactions track income (positive amounts) and expenses (negative amounts).
 */
@Getter
@Setter
@Document(collection = "transactions")
public class Transaction extends AbstractEntity {

    @Indexed
    private String userId;

    private String userUsername;

    @NotBlank(message = "Category ID is required")
    @Indexed
    private String categoryId;

    private String categoryName;
    private String categoryColor;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @NotNull(message = "Date is required")
    @Indexed
    private LocalDate date;
}
