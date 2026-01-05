package com.budgettracker.budget_tracker_backend.model.auth;

import com.budgettracker.budget_tracker_backend.model.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a capability/permission in the system.
 * Capabilities are assigned to roles for access control.
 */
@Getter
@Setter
@Document(collection = "capabilities")
public class Capability extends AbstractEntity {

    @Indexed(unique = true)
    private String name;
    private String description;
}
