package com.budgettracker.budget_tracker_backend.core;

import com.budgettracker.budget_tracker_backend.core.exceptions.*;
import com.budgettracker.budget_tracker_backend.dto.common.ResponseMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for @Controller/@RestController methods.
 * Returns consistent JSON error responses with appropriate HTTP status codes.
 */
@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Catch-all handler for all business exceptions that extend AppGenericException.
     * This serves as a safety net for any AppGenericException that doesn't have a
     * more specific handler. It uses the determineHttpStatus() helper to map
     * exception types to appropriate HTTP status codes.
     */
    @ExceptionHandler(AppGenericException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppGenericException(AppGenericException e) {
        log.warn("Application exception: Code={}, Message={}", e.getErrorCode(), e.getMessage());

        HttpStatus status = determineHttpStatus(e);
        return ResponseEntity
                .status(status)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles "not found" business exceptions (HTTP 404).
     * Example: Requested resource doesn't exist in the database.
     */
    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleNotFound(AppObjectNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles invalid argument business exceptions (HTTP 400).
     * Example: Invalid input parameters that violate business rules.
     */
    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleInvalidArgument(AppObjectInvalidArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles "already exists" business exceptions (HTTP 409 - Conflict).
     * Example: Trying to create a resource that already exists (duplicate email, username, etc.).
     */
    @ExceptionHandler(AppObjectAlreadyExistsException.class)
    public ResponseEntity<ResponseMessageDTO> handleAlreadyExists(AppObjectAlreadyExistsException e) {
        log.warn("Entity already exists: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles authorization failures for business objects (HTTP 403 - Forbidden).
     * Example: User tries to access a resource they don't own or have permission for.
     */
    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleNotAuthorized(AppObjectNotAuthorizedException e) {
        log.warn("Authorization failed: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles internal server errors from business logic (HTTP 500).
     * This should be used for unexpected internal errors that are not
     * the user's fault (e.g., database connection failures, external API failures).
     */
    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleServerException(AppServerException e) {
        log.error("Server error: Code={}, Message={}", e.getErrorCode(), e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO(e.getErrorCode(), e.getMessage()));
    }

    /**
     * Handles custom validation exceptions from service layer (HTTP 400).
     * Returns field-specific error messages in a map structure.
     * Example: { "email": "Email must be valid", "password": "Password too short" }
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        log.warn("Validation failed: {}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Spring's @Valid annotation validation failures (HTTP 400).
     * This is called automatically when @Valid fails on controller method parameters.
     * Overrides the default Spring behavior to provide consistent error format.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nullable org.springframework.http.HttpHeaders headers,
            @Nullable org.springframework.http.HttpStatusCode status,
            @Nullable WebRequest request) {

        log.warn("Request validation failed");
        BindingResult bindingResult = ex.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles IO exceptions (HTTP 500).
     * Example: File upload failures, stream processing errors.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseMessageDTO> handleIOException(IOException e) {
        log.error("IO error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("IO_ERROR", "An input/output error occurred"));
    }

    /**
     * Handles Spring Security access denied exceptions (HTTP 403).
     * This is triggered by @PreAuthorize, @PostAuthorize annotations or
     * manual security checks in controllers.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseMessageDTO> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessageDTO("ACCESS_DENIED",
                        "You don't have permission to access this resource"));
    }

    /**
     * Handles database access exceptions (HTTP 500).
     * Example: DB exceptions, connection pool errors, transaction failures.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseMessageDTO> handleDatabaseErrors(DataAccessException e) {
        log.error("Database error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("DATABASE_ERROR",
                        "A database error occurred"));
    }

    /**
     * Ultimate catch-all handler for any exception not caught above (HTTP 500).
     * This prevents Spring's default white-label error page and ensures
     * all exceptions return consistent JSON format.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessageDTO> handleUnhandledException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("INTERNAL_ERROR",
                        "An unexpected error occurred"));
    }

    /**
     * Maps AppGenericException subtypes to appropriate HTTP status codes.
     * This ensures consistent status code mapping even when using the
     * generic handleAppGenericException() method.
     *
     * @param e The AppGenericException to map
     * @return The appropriate HTTP status code
     */
    private HttpStatus determineHttpStatus(AppGenericException e) {
        return switch (e) {
            case AppObjectNotFoundException x -> HttpStatus.NOT_FOUND;
            case AppObjectInvalidArgumentException x -> HttpStatus.BAD_REQUEST;
            case AppObjectAlreadyExistsException x -> HttpStatus.CONFLICT;
            case AppObjectNotAuthorizedException x -> HttpStatus.FORBIDDEN;
            case AppServerException x -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST; // Default for other AppGenericException subtypes
        };
    }
}