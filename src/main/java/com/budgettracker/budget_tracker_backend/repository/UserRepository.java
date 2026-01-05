package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for managing {@link User} entities in MongoDB.
 * Handles core authentication queries (find by username/email) and validation for user registration.
 */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
