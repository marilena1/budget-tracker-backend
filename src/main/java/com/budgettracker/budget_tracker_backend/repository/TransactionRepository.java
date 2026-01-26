package com.budgettracker.budget_tracker_backend.repository;

import com.budgettracker.budget_tracker_backend.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing {@link Transaction} entities in MongoDB.
 * Provides core queries for filtering transactions by user, category, and date ranges.
 * Supports both paginated (Page) and non-paginated (List) query results.
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);
    List<Transaction> findByUserIdAndCategoryId(String userId, String categoryId);
    List<Transaction> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);

    Page<Transaction> findByUserId(String userId, Pageable pageable);
    Page<Transaction> findByUserIdAndCategoryId(String userId, String categoryId, Pageable pageable);
    Page<Transaction> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end, Pageable pageable);

    // For type filtering (income = positive, expense = negative)
    Page<Transaction> findByUserIdAndAmountGreaterThan(String userId, BigDecimal amount, Pageable pageable);
    Page<Transaction> findByUserIdAndAmountLessThan(String userId, BigDecimal amount, Pageable pageable);

    // For category + date range
    Page<Transaction> findByUserIdAndCategoryIdAndDateBetween(
            String userId, String categoryId, LocalDate start, LocalDate end, Pageable pageable);

    // For category + type
    Page<Transaction> findByUserIdAndCategoryIdAndAmountGreaterThan(
            String userId, String categoryId, BigDecimal amount, Pageable pageable);
    Page<Transaction> findByUserIdAndCategoryIdAndAmountLessThan(
            String userId, String categoryId, BigDecimal amount, Pageable pageable);

    // For date range + type
    Page<Transaction> findByUserIdAndDateBetweenAndAmountGreaterThan(
            String userId, LocalDate start, LocalDate end, BigDecimal amount, Pageable pageable);
    Page<Transaction> findByUserIdAndDateBetweenAndAmountLessThan(
            String userId, LocalDate start, LocalDate end, BigDecimal amount, Pageable pageable);

    // For all three filters
    Page<Transaction> findByUserIdAndCategoryIdAndDateBetweenAndAmountGreaterThan(
            String userId, String categoryId, LocalDate start, LocalDate end, BigDecimal amount, Pageable pageable);
    Page<Transaction> findByUserIdAndCategoryIdAndDateBetweenAndAmountLessThan(
            String userId, String categoryId, LocalDate start, LocalDate end, BigDecimal amount, Pageable pageable);

    boolean existsByCategoryId(String categoryId);
}
