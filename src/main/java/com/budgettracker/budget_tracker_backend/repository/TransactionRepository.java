package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing {@link Transaction} entities in MongoDB.
 * Provides core queries for filtering transactions by user, category, and date ranges.
 */
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);
    List<Transaction> findByUserIdAndCategoryId(String userId, String categoryId);
    List<Transaction> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
}
