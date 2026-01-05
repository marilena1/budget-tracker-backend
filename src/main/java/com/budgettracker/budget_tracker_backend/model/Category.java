package com.budgettracker.budget_tracker_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a transaction category for organizing financial transactions.
 * Categories help users group and analyze their income and expenses.
 */
@Getter
@Setter
@Document(collection = "categories")
public class Category extends AbstractEntity {

    @Indexed(unique = true)
    private String name;

    private String description;
    private String color;
}
