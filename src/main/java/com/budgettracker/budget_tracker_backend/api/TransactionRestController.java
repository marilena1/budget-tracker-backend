package com.budgettracker.budget_tracker_backend.api;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotAuthorizedException;
import com.budgettracker.budget_tracker_backend.core.exceptions.ValidationException;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.dto.transaction.TransactionSummaryReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

/**
 * REST Controller for managing financial transaction operations.
 * Provides endpoints for CRUD operations and filtering of transactions.
 * All endpoints are prefixed with "/api" and require appropriate authentication.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionRestController {

    private final ITransactionService transactionService;

    /**
     * Creates a new financial transaction for the specified user.
     * Validates the transaction data and user ownership before creation.
     * Returns the created transaction with a location header containing its URI.
     *
     * @param transactionInsertDTO the transaction data to create, validated for constraints
     * @param username the username of the user creating the transaction
     * @param bindingResult contains validation errors for the transactionInsertDTO
     * @return ResponseEntity containing the created TransactionReadOnlyDTO with HTTP 201 status
     *         and location header pointing to the new resource
     * @throws ValidationException if the transactionInsertDTO fails validation constraints
     * @throws AppObjectInvalidArgumentException if transaction data violates business rules
     * @throws AppObjectNotFoundException if the referenced category or user does not exist
     */
    @Operation(
            summary = "Create a new transaction",
            description = "Creates a new financial transaction for the specified user with validated data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = TransactionReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "404", description = "Referenced category or user not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PostMapping
    public ResponseEntity<TransactionReadOnlyDTO> createTransaction(
            @Valid @RequestBody TransactionInsertDTO transactionInsertDTO,
            @RequestParam @NotBlank(message = "Username is required") String username,
            BindingResult bindingResult)
            throws AppObjectInvalidArgumentException, AppObjectNotFoundException, ValidationException {

        log.info("CREATE TRANSACTION REQUEST - User: {}, Category: {}, Amount: {}",
                username, transactionInsertDTO.categoryId(), transactionInsertDTO.amount());

        if (bindingResult.hasErrors()) {
            log.warn(" Transaction creation failed - Validation errors for user: {}", username);
            throw new ValidationException(bindingResult);
        }

        TransactionReadOnlyDTO transactionReadOnlyDTO =
                transactionService.createTransaction(transactionInsertDTO, username);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transactionReadOnlyDTO.id())
                .toUri();

        log.info("Transaction created successfully - ID: {}, User: {}, Location: {}",
                transactionReadOnlyDTO.id(), username, location);


        return ResponseEntity.created(location).body(transactionReadOnlyDTO);
    }

    /**
     * Updates an existing transaction with new data.
     * Validates that the transaction exists, the user owns it, and the new data is valid.
     *
     * @param transactionId the unique identifier of the transaction to update
     * @param transactionUpdateDTO the new transaction data to apply
     * @param username the username of the user attempting the update
     * @param bindingResult contains validation errors for the transactionUpdateDTO
     * @return ResponseEntity containing the updated TransactionReadOnlyDTO
     * @throws ValidationException if the transactionUpdateDTO fails validation constraints
     * @throws AppObjectNotFoundException if the transaction with given ID does not exist
     * @throws AppObjectNotAuthorizedException if the user does not own the transaction
     * @throws AppObjectInvalidArgumentException if the update data violates business rules
     */
    @Operation(
            summary = "Update an existing transaction",
            description = "Updates an existing transaction with new data after validating ownership and constraints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this transaction")
    })
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionReadOnlyDTO> updateTransaction(
            @PathVariable @NotBlank(message = "Transaction ID is required") String transactionId,
            @Valid @RequestBody TransactionInsertDTO transactionUpdateDTO,
            @RequestParam @NotBlank(message = "Username is required") String username,
            BindingResult bindingResult)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException,
            AppObjectInvalidArgumentException, ValidationException {

        log.info("UPDATE TRANSACTION REQUEST - ID: {}, User: {}, New Amount: {}",
                transactionId, username, transactionUpdateDTO.amount());

        if (bindingResult.hasErrors()) {
            log.warn("Transaction update failed - Validation errors for ID: {}, User: {}",
                    transactionId, username);
            throw new ValidationException(bindingResult);
        }

        TransactionReadOnlyDTO updatedTransaction =
                transactionService.updateTransaction(transactionId, transactionUpdateDTO, username);

        log.info("Transaction updated successfully - ID: {}, User: {}", transactionId, username);

        return ResponseEntity.ok(updatedTransaction);
    }

    /**
     * Deletes a specific transaction belonging to a user.
     * Validates that the transaction exists and that the user owns it.
     *
     * @param username the username of the user attempting the deletion
     * @param transactionId the ID of the transaction to delete
     * @return ResponseEntity with HTTP 204 No Content status upon successful deletion
     * @throws AppObjectNotFoundException if the transaction does not exist
     * @throws AppObjectNotAuthorizedException if the user does not own the transaction
     */
    @Operation(
            summary = "Delete a transaction",
            description = "Deletes a specific transaction after validating user ownership."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete this transaction")
    })
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @RequestParam @NotBlank(message = "Username is required") String username,
            @PathVariable @NotBlank(message = "Transaction ID is required") String transactionId)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        log.info("DELETE TRANSACTION REQUEST - ID: {}, User: {}", transactionId, username);

        transactionService.deleteTransaction(username, transactionId);

        log.info("Transaction deleted successfully - ID: {}, User: {}", transactionId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves aggregated transaction summary for a specific user.
     * Includes total income, total expenses, net balance, recent transactions,
     * and top spending categories. This endpoint is optimized for dashboard
     * display and does not use pagination as it returns calculated aggregates.
     *
     * @param username the username of the user whose transaction summary to retrieve
     * @return ResponseEntity containing a TransactionSummaryReadOnlyDTO with aggregated financial data
     * @throws AppObjectNotFoundException if the user does not exist
     */
    @Operation(
            summary = "Get transaction summary for dashboard",
            description = "Retrieves aggregated transaction data including totals, recent transactions, " +
                    "and category breakdowns. Optimized for dashboard display without pagination."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TransactionSummaryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/user/{username}/summary")
    public ResponseEntity<TransactionSummaryReadOnlyDTO> getTransactionSummary(
            @PathVariable @NotBlank(message = "Username is required") String username)
            throws AppObjectNotFoundException {

        log.info("GET TRANSACTION SUMMARY REQUEST - User: {}", username);

        TransactionSummaryReadOnlyDTO summary = transactionService.getTransactionSummary(username);

        log.info("Transaction summary retrieved - User: {}, Income: {}, Expenses: {}, Balance: {}",
                username, summary.totalIncome(), summary.totalExpenses(), summary.netBalance());

        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieves a paginated list of transactions for a specific user.
     * Results are ordered by date descending (newest first) and then by creation time.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param page the page number (0-based)
     * @param size the number of transactions per page
     * @return ResponseEntity containing a Page of TransactionReadOnlyDTOs for the specified user
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectInvalidArgumentException if page is negative or size is not between 1 and 100
     */
    @Operation(
            summary = "Get transactions by user",
            description = "Retrieves paginated transactions for a specific user, ordered by date descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getTransactionsByUser(
            @PathVariable @NotBlank(message = "Username is required") String username,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.debug("GET TRANSACTIONS REQUEST - User: {}, Page: {}, Size: {}", username, page, size);

        Page<TransactionReadOnlyDTO> transactions =
                transactionService.getTransactionsByUser(username, page, size);

        log.debug("Transactions retrieved - User: {}, Count: {}, Total Pages: {}",
                username, transactions.getNumberOfElements(), transactions.getTotalPages());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Retrieves transactions for a user within a specific date range.
     * Useful for generating monthly statements or period-based reports.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param startDate the start date of the range (inclusive), format: yyyy-MM-dd
     * @param endDate the end date of the range (inclusive), format: yyyy-MM-dd
     * @param page the page number (0-based)
     * @param size the number of transactions per page
     * @return ResponseEntity containing a Page of TransactionReadOnlyDTOs filtered by date range
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid,
     *         dates are null, startDate is after endDate, or date range exceeds reasonable limits
     */
    @Operation(
            summary = "Get transactions by date range",
            description = "Retrieves paginated transactions for a user within a specific date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or pagination parameters")
    })
    @GetMapping("/user/{username}/date-range")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getTransactionsByUserAndDateRange(
            @PathVariable @NotBlank(message = "Username is required") String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
    throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.info("GET TRANSACTIONS BY DATE RANGE - User: {}, Start: {}, End: {}, Page: {}, Size: {}",
                username, startDate, endDate, page, size);

        Page<TransactionReadOnlyDTO> transactions =
                transactionService.getTransactionsByUserAndDateRange(username, startDate, endDate, page, size);

        log.info("Date range transactions retrieved - User: {}, Count: {}, Date Range: {} to {}",
                username, transactions.getNumberOfElements(), startDate, endDate);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Retrieves transactions for a user filtered by a specific category.
     * Helpful for analyzing spending in particular categories (e.g., "Food", "Entertainment").
     *
     * @param username the username of the user whose transactions to retrieve
     * @param categoryId the ID of the category to filter by
     * @param page the page number (0-based)
     * @param size the number of transactions per page
     * @return ResponseEntity containing a Page of TransactionReadOnlyDTOs filtered by category
     * @throws AppObjectNotFoundException if the specified category does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    @Operation(
            summary = "Get transactions by category",
            description = "Retrieves paginated transactions for a user filtered by a specific category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User or category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/user/{username}/category/{categoryId}")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getTransactionsByUserAndCategory(
            @PathVariable @NotBlank(message = "Username is required") String username,
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.info("GET TRANSACTIONS BY CATEGORY - User: {}, Category ID: {}, Page: {}, Size: {}",
                username, categoryId, page, size);

        Page<TransactionReadOnlyDTO> transactions =
                transactionService.getTransactionsByUserAndCategory(username, categoryId, page, size);

        log.info("Category transactions retrieved - User: {}, Category: {}, Count: {}",
                username, categoryId, transactions.getNumberOfElements());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Retrieves transactions for a user with multiple optional filters.
     * Supports filtering by category, transaction type (income/expense), date range,
     * and pagination. All filters are optional - omitted filters are not applied.
     * Type filtering: "income" for positive amounts, "expense" for negative amounts.
     *
     * @param username the username of the user whose transactions to retrieve
     * @param categoryId optional category ID to filter by (null for all categories)
     * @param type optional transaction type to filter by ("income", "expense", or null for both)
     * @param startDate optional start date of the range (inclusive), format: yyyy-MM-dd
     * @param endDate optional end date of the range (inclusive), format: yyyy-MM-dd
     * @param page the page number (0-based)
     * @param size the number of transactions per page
     * @return ResponseEntity containing a Page of TransactionReadOnlyDTOs filtered by the specified criteria
     * @throws AppObjectNotFoundException if the user or specified category does not exist
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid,
     *         startDate is after endDate, or type is not "income" or "expense"
     */
    @Operation(
            summary = "Get transactions with multiple filters",
            description = "Retrieves paginated transactions for a user with optional filters " +
                    "by category, type (income/expense), and date range. Type is determined by amount: " +
                    "positive for income, negative for expense. All filters are optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User or category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters")
    })
    @GetMapping("/user/{username}/filter")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getTransactionsByUserWithFilters(
            @PathVariable @NotBlank(message = "Username is required") String username,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.info("GET TRANSACTIONS WITH FILTERS - User: {}, Category: {}, Type: {}, Start: {}, End: {}, Page: {}, Size: {}",
                username, categoryId, type, startDate, endDate, page, size);

        Page<TransactionReadOnlyDTO> transactions =
                transactionService.getTransactionsByUserWithFilters(
                        username, categoryId, type, startDate, endDate, page, size);

        log.info("Filtered transactions retrieved - User: {}, Count: {}, Filters: Category={}, Type={}, DateRange={} to {}",
                username, transactions.getNumberOfElements(), categoryId, type, startDate, endDate);

        return ResponseEntity.ok(transactions);
    }
}