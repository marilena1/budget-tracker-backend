package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Category} entities in MongoDB.
 * Categories organize transactions (e.g., "Food", "Rent"). Provides methods for sorted retrieval and duplicate prevention.
 */
public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    @Query("{ 'name': ?0, '_id': { $ne: ?1 } }")
    boolean existsByNameAndIdNot(String name, String id);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Category> findByNameContainingIgnoreCase(String nameQuery);
}
