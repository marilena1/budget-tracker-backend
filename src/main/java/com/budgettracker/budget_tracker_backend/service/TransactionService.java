package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotAuthorizedException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.model.Category;
import com.budgettracker.budget_tracker_backend.model.Transaction;
import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.repository.CategoryRepository;
import com.budgettracker.budget_tracker_backend.repository.TransactionRepository;
import com.budgettracker.budget_tracker_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Service implementation for transaction management operations.
 * Contains business logic for creating, reading, updating, and deleting transactions.
 * Handles data validation, authorization checks, and entity-DTO conversions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new financial transaction for the specified user.
     * Validates business rules, ensures referenced entities exist, and denormalizes
     * related data for query performance.
     *
     * @param transactionInsertDTO the transaction data provided by the user
     * @param username the username of the user creating the transaction
     * @return TransactionReadOnlyDTO containing the created transaction with system-generated fields
     * @throws AppObjectInvalidArgumentException if transaction data violates business rules
     * @throws AppObjectNotFoundException if referenced category or user does not exist
     */
    @Override
    public TransactionReadOnlyDTO createTransaction(TransactionInsertDTO transactionInsertDTO, String username)
            throws AppObjectInvalidArgumentException, AppObjectNotFoundException {

        log.info("Creating transaction for user '{}' with amount {} in category {}",
                username, transactionInsertDTO.amount(), transactionInsertDTO.categoryId());

        if (transactionInsertDTO.amount() == null || transactionInsertDTO.amount().compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Transaction creation failed: amount is zero or null for user '{}'", username);
            throw new AppObjectInvalidArgumentException("amount", "cannot be zero");
        }

        Category category = categoryRepository.findById(transactionInsertDTO.categoryId())
                .orElseThrow(() -> {
                    log.warn("Category {} not found for user '{}'",
                            transactionInsertDTO.categoryId(), username);
                    return new AppObjectNotFoundException("Category", transactionInsertDTO.categoryId());
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found during transaction creation", username);
                    return new AppObjectNotFoundException("User", username);
                });

        String userId = user.getId();

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setUserUsername(user.getUsername());
        transaction.setCategoryId(transactionInsertDTO.categoryId());
        transaction.setCategoryName(category.getName());
        transaction.setCategoryColor(category.getColor());
        transaction.setAmount(transactionInsertDTO.amount());
        transaction.setDescription(transactionInsertDTO.description());
        transaction.setDate(transactionInsertDTO.date());

        log.debug("Saving transaction for user '{}' with ID: {}", username, user.getId());

        Transaction saved = transactionRepository.save(transaction);

        log.info("Transaction created successfully. ID: {}, User: {}, Amount: {}, Category: {}",
                saved.getId(), username, saved.getAmount(), saved.getCategoryName());

        return TransactionReadOnlyDTO.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .userUsername(saved.getUserUsername())
                .categoryId(saved.getCategoryId())
                .categoryName(saved.getCategoryName())
                .categoryColor(saved.getCategoryColor())
                .amount(saved.getAmount())
                .description(saved.getDescription())
                .date(saved.getDate())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();

    }

    /**
     * Updates an existing transaction with new data.
     * Validates transaction existence, user ownership, and business rules.
     *
     * @param transactionId the unique identifier of the transaction to update
     * @param transactionUpdateDTO the new transaction data
     * @param username the username of the user attempting the update
     * @return TransactionReadOnlyDTO containing the updated transaction
     * @throws AppObjectNotFoundException if transaction, referenced category, or user does not exist
     * @throws AppObjectNotAuthorizedException if user does not own the transaction
     * @throws AppObjectInvalidArgumentException if update data violates business rules
     */
    @Override
    public TransactionReadOnlyDTO updateTransaction(String transactionId, TransactionInsertDTO transactionUpdateDTO, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        log.info("Updating transaction {} for user '{}'", transactionId, username);

        if (transactionUpdateDTO.amount() == null || transactionUpdateDTO.amount().compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Transaction update failed: amount is zero or null for transaction {} by user '{}'",
                    transactionId, username);
            throw new AppObjectInvalidArgumentException("amount", "cannot be zero");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found during transaction update for transaction {}",
                            username, transactionId);
                    return new AppObjectNotFoundException("User", username);
                });
        String userId = user.getId();

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction {} not found for update by user '{}'",
                            transactionId, username);
                    return new AppObjectNotFoundException("Transaction", transactionId);
                });

        if (!existingTransaction.getUserId().equals(userId)) {
            log.warn("User '{}' attempted to update transaction {} belonging to user '{}'",
                    username, transactionId, existingTransaction.getUserUsername());
            throw new AppObjectNotAuthorizedException(
                    "Transaction " + transactionId,
                    "You must be the owner of a transaction to update it"
            );
        }

        Category category;
        boolean categoryChanged = !existingTransaction.getCategoryId().equals(transactionUpdateDTO.categoryId());

        if (categoryChanged) {
            log.debug("Category changed for transaction {} from {} to {}",
                    transactionId, existingTransaction.getCategoryId(), transactionUpdateDTO.categoryId());
            category = categoryRepository.findById(transactionUpdateDTO.categoryId())
                    .orElseThrow(() -> {
                        log.warn("Category {} not found during update of transaction {} by user '{}'",
                                transactionUpdateDTO.categoryId(), transactionId, username);
                        return new AppObjectNotFoundException("Category", transactionUpdateDTO.categoryId());
                    });
        } else {
            category = categoryRepository.findById(existingTransaction.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("Existing category {} not found for transaction {}",
                                existingTransaction.getCategoryId(), transactionId);
                        return new AppObjectNotFoundException("Category", existingTransaction.getCategoryId());
                    });
        }

        existingTransaction.setCategoryId(transactionUpdateDTO.categoryId());
        existingTransaction.setCategoryName(category.getName());
        existingTransaction.setCategoryColor(category.getColor());
        existingTransaction.setAmount(transactionUpdateDTO.amount());
        existingTransaction.setDescription(transactionUpdateDTO.description());
        existingTransaction.setDate(transactionUpdateDTO.date());

        log.debug("Saving updated transaction {} for user '{}'", transactionId, username);

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        log.info("Transaction {} updated successfully by user '{}'", transactionId, username);

        return TransactionReadOnlyDTO.builder()
                .id(updatedTransaction.getId())
                .userId(updatedTransaction.getUserId())
                .userUsername(updatedTransaction.getUserUsername())
                .categoryId(updatedTransaction.getCategoryId())
                .categoryName(updatedTransaction.getCategoryName())
                .categoryColor(updatedTransaction.getCategoryColor())
                .amount(updatedTransaction.getAmount())
                .description(updatedTransaction.getDescription())
                .date(updatedTransaction.getDate())
                .createdAt(updatedTransaction.getCreatedAt())
                .updatedAt(updatedTransaction.getUpdatedAt())
                .build();
    }

    /**
     * Deletes a specific transaction belonging to a user.
     * Validates that the transaction exists and that the user owns it.
     *
     * @param username the username of the user attempting the deletion
     * @param transactionId the ID of the transaction to delete
     * @throws AppObjectNotFoundException if the transaction or user does not exist
     * @throws AppObjectNotAuthorizedException if the user does not own the transaction
     */
    @Override
    public void deleteTransaction(String username, String transactionId)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        log.info("Deleting transaction {} for user '{}'", transactionId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found during deletion of transaction {}",
                            username, transactionId);
                    return new AppObjectNotFoundException("User", username);
                });
        String userId = user.getId();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction {} not found for deletion by user '{}'",
                            transactionId, username);
                    return new AppObjectNotFoundException("Transaction", transactionId);
                });

        if (!transaction.getUserId().equals(userId)) {
            log.warn("User '{}' attempted to delete transaction {} belonging to user '{}'",
                    username, transactionId, transaction.getUserUsername());
            throw new AppObjectNotAuthorizedException(
                    "Transaction " + transactionId,
                    "You must be the owner of a transaction to delete it");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully by user '{}'", transactionId, username);
    }

        /**
         * Retrieves a paginated list of transactions for a specific user.
         * Results are ordered by date descending (newest first) and then by creation time.
         * Validates pagination parameters before querying the database.
         *
         * @param username the username of the user whose transactions to retrieve
         * @param page the page number (0-based)
         * @param size the number of transactions per page (maximum 100)
         * @return Page of TransactionReadOnlyDTOs for the specified user
         * @throws AppObjectNotFoundException if the user does not exist
         * @throws AppObjectInvalidArgumentException if page is negative or size is not between 1 and 100
         */
    @Override
    public Page<TransactionReadOnlyDTO> getPaginatedTransactionsByUser(String username, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.debug("Getting paginated transactions for user '{}', page={}, size={}",
                username, page, size);

        if (page < 0) {
            log.warn("Invalid page number {} for user '{}'", page, username);
            throw new AppObjectInvalidArgumentException("page", "must be zero or positive");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size {} for user '{}'", size, username);
            throw new AppObjectInvalidArgumentException("size", "must be between 1 and 100");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found when fetching paginated transactions", username);
                    return new AppObjectNotFoundException("User", username);
                });
        String userId = user.getId();


        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending()
                .and(Sort.by("createdAt").descending()));

        Page<Transaction> transactionsPage = transactionRepository.findByUserId(userId, pageable);

        log.debug("Returning {} transactions for user '{}', page {} of {}",
                transactionsPage.getNumberOfElements(), username, page, transactionsPage.getTotalPages());

        return transactionsPage.map(transaction -> TransactionReadOnlyDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userUsername(transaction.getUserUsername())
                .categoryId(transaction.getCategoryId())
                .categoryName(transaction.getCategoryName())
                .categoryColor(transaction.getCategoryColor())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build());
    }

    /**
     * Retrieves transactions for a user within a specific date range.
     * Useful for generating monthly statements or period-based reports.
     * Validates pagination parameters and date range validity before querying.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param startDate the start date of the range (inclusive), cannot be null
     * @param endDate the end date of the range (inclusive), cannot be null
     * @param page the page number (0-based)
     * @param size the number of transactions per page, must be between 1 and 100
     * @return Page of TransactionReadOnlyDTOs filtered by date range
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid,
     *         dates are null, startDate is after endDate, or date range exceeds reasonable limits
     */
    @Override
    public Page<TransactionReadOnlyDTO> getTransactionByUserAndDateRange(String username, LocalDate startDate, LocalDate endDate, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.debug("Getting transactions for user '{}' between {} and {}, page={}, size={}",
                username, startDate, endDate, page, size);

        if (page < 0) {
            log.warn("Invalid page number {} for user '{}' date range query", page, username);
            throw new AppObjectInvalidArgumentException("page", "must be zero or positive");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size {} for user '{}' date range query", size, username);
            throw new AppObjectInvalidArgumentException("size", "must be between 1 and 100");
        }
        if (startDate == null) {
            log.warn("Null startDate for user '{}' date range query", username);
            throw new AppObjectInvalidArgumentException("startDate", "cannot be null");
        }
        if (endDate == null) {
            log.warn("Null endDate for user '{}' date range query", username);
            throw new AppObjectInvalidArgumentException("endDate", "cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range for user '{}': startDate {} is after endDate {}",
                    username, startDate, endDate);
            throw new AppObjectInvalidArgumentException("date range", "startDate must be before endDate");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found when fetching date range transactions", username);
                    return new AppObjectNotFoundException("User", username);
                });
        String userId = user.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending()
                .and(Sort.by("createdAt").descending()));

        Page<Transaction> transactionsPage = transactionRepository.findByUserIdAndDateBetween(
                userId, startDate, endDate, pageable);

        log.debug("Returning {} transactions for user '{}' between {} and {}",
                transactionsPage.getNumberOfElements(), username, startDate, endDate);

        return transactionsPage.map(transaction -> TransactionReadOnlyDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userUsername(transaction.getUserUsername())
                .categoryId(transaction.getCategoryId())
                .categoryName(transaction.getCategoryName())
                .categoryColor(transaction.getCategoryColor())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build());
    }

    /**
     * Retrieves transactions for a user filtered by a specific category.
     * Helpful for analyzing spending in particular categories.
     * Validates pagination parameters and verifies category existence.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param categoryId the ID of the category to filter by, must reference an existing category
     * @param page the page number (0-based)
     * @param size the number of transactions per page, must be between 1 and 100
     * @return Page of TransactionReadOnlyDTOs filtered by category
     * @throws AppObjectNotFoundException if the specified category does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    @Override
    public Page<TransactionReadOnlyDTO> getTransactionByUserAndCategory(String username, String categoryId, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.debug("Getting transactions for user '{}' in category {}, page={}, size={}",
                username, categoryId, page, size);

        if (page < 0) {
            log.warn("Invalid page number {} for user '{}' category query", page, username);
            throw new AppObjectInvalidArgumentException("page", "must be zero or positive");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size {} for user '{}' category query", size, username);
            throw new AppObjectInvalidArgumentException("size", "must be between 1 and 100");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found when fetching category transactions", username);
                    return new AppObjectNotFoundException("User", username);
                });
        String userId = user.getId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category {} not found for user '{}' category transactions",
                            categoryId, username);
                    return new AppObjectNotFoundException("Category", categoryId);
                });

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending()
                .and(Sort.by("createdAt").descending()));

        Page<Transaction> transactionsPage = transactionRepository.findByUserIdAndCategoryId(
                userId, categoryId, pageable);

        log.debug("Returning {} transactions for user '{}' in category {}",
                transactionsPage.getNumberOfElements(), username, category.getName());

        return transactionsPage.map(transaction -> TransactionReadOnlyDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .userUsername(transaction.getUserUsername())
                .categoryId(transaction.getCategoryId())
                .categoryName(transaction.getCategoryName())
                .categoryColor(transaction.getCategoryColor())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build());
    }
}
