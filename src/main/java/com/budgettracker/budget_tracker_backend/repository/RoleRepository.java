package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.auth.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for managing {@link Role} entities in MongoDB.
 * Roles group capabilities and are assigned to users for access control.
 */
public interface RoleRepository extends MongoRepository<Role, String> {

    boolean existsByName(String name);

    Optional<Role> findByName(String name);
}
