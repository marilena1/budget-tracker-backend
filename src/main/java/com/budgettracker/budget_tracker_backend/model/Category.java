package com.budgettracker.budget_tracker_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    @Indexed(unique = true)
    private String name;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private String color;
}
