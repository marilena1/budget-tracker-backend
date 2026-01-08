package com.budgettracker.budget_tracker_backend.model;

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

    @Indexed
    private String categoryId;

    private String categoryName;

    private String categoryColor;

    private BigDecimal amount;

    private String description;

    @Indexed
    private LocalDate date;
}
