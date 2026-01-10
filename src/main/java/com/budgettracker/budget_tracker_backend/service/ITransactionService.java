package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotAuthorizedException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionReadOnlyDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * Service interface for managing financial transactions.
 * Defines the business operations for transaction CRUD and filtering.
 */
public interface ITransactionService {

    /**
     * Creates a new financial transaction for the specified user.
     * Validates business rules and ensures the referenced category exists.
     *
     * @param transactionInsertDTO the transaction data to create
     * @param username the username of the user creating the transaction
     * @return TransactionReadOnlyDTO containing the created transaction data
     * @throws AppObjectInvalidArgumentException if transaction data violates business rules
     * @throws AppObjectNotFoundException if the referenced category or user does not exist
     */
    TransactionReadOnlyDTO createTransaction(TransactionInsertDTO transactionInsertDTO, String username)
        throws AppObjectInvalidArgumentException, AppObjectNotFoundException;

    /**
     * Updates an existing transaction with new data.
     * Validates that the transaction exists, the user owns it, and the new data is valid.
     *
     * @param transactionId the unique identifier of the transaction to update
     * @param transactionUpdateDTO the new transaction data to apply (uses same structure as creation)
     * @param username the username of the user attempting the update (for ownership verification)
     * @return TransactionReadOnlyDTO containing the updated transaction data
     * @throws AppObjectNotFoundException if the transaction with given ID does not exist
     * @throws AppObjectNotAuthorizedException if the user does not own the transaction
     * @throws AppObjectInvalidArgumentException if the update data violates business rules
     *         (e.g., invalid amount, future date, etc.)
     */
    TransactionReadOnlyDTO updateTransaction(String transactionId, TransactionInsertDTO transactionUpdateDTO, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException;

    /**
     * Deletes a specific transaction belonging to a user.
     * Validates that the transaction exists and that the user owns it.
     *
     * @param username the username of the user attempting the deletion
     * @param transactionId the ID of the transaction to delete
     * @throws AppObjectNotFoundException if the transaction does not exist
     * @throws AppObjectNotAuthorizedException if the user does not own the transaction
     */
    void deleteTransaction(String username, String transactionId)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException;

    /**
     * Retrieves a paginated list of transactions for a specific user.
     * Results are ordered by date descending (newest first) and then by creation time.
     * Includes validation for pagination parameters to ensure valid database queries.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param page the page number (0-based)
     * @param size the number of transactions per page, must be between 1 and 100
     * @return Page of TransactionReadOnlyDTOs for the specified user
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectInvalidArgumentException if page is negative or size is not between 1 and 100
     */
    Page<TransactionReadOnlyDTO> getTransactionsByUser(String username, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException;

    /**
     * Retrieves transactions for a user within a specific date range.
     * Useful for generating monthly statements or period-based reports.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @param page the page number (0-based)
     * @param size the number of transactions per page, must be between 1 and 100
     * @return Page of TransactionReadOnlyDTOs filtered by date range
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid,
     *         dates are null, startDate is after endDate, or date range exceeds reasonable limits
     */
    Page<TransactionReadOnlyDTO> getTransactionsByUserAndDateRange(String username, LocalDate startDate, LocalDate endDate, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException ;

    /**
     * Retrieves transactions for a user filtered by a specific category.
     * Helpful for analyzing spending in particular categories (e.g., "Food", "Entertainment").
     *
     * @param username the username of the user whose transactions to retrieve
     * @param categoryId the ID of the category to filter by
     * @param page the page number (0-based)
     * @param size the number of transactions per page, must be between 1 and 100
     * @return Page of TransactionReadOnlyDTOs filtered by category
     * @throws AppObjectNotFoundException if the specified category does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    Page<TransactionReadOnlyDTO> getTransactionsByUserAndCategory(String username, String categoryId, int page, int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException;
}
