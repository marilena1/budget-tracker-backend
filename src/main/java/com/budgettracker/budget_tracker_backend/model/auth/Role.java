package com.budgettracker.budget_tracker_backend.model.auth;

import com.budgettracker.budget_tracker_backend.model.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user role in the system.
 * Roles are assigned to users and contain capabilities/permissions.
 */
@Getter
@Setter
@Document(collection = "roles")
public class Role extends AbstractEntity {

    @Indexed(unique = true)
    private String name;

    private String description;

    private Set<String> capabilityNames = new HashSet<>();


    /**
     * Adds a capability to this role.
     * @param capabilityName the name of the capability to add
     */
    public void addCapability(String capabilityName) {
        this.capabilityNames.add(capabilityName);
    }

    /**
     * Removes a capability from this role.
     * @param capabilityName the name of the capability to remove
     */
    public void removeCapability(String capabilityName) {
        this.capabilityNames.remove(capabilityName);
    }
}