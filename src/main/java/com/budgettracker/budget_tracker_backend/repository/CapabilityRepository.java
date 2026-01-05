package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.auth.Capability;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CapabilityRepository extends MongoRepository<Capability, String> {

    boolean existsByName(String name);

    Optional<Capability> findByName(String name);
}