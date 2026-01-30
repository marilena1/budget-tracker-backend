package com.budgettracker.budget_tracker_backend.api;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.core.exceptions.ValidationException;
import com.budgettracker.budget_tracker_backend.dto.user.UserInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.user.UserReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST Controller for managing user account operations.
 * Provides endpoints for user registration, profile management, and administrative functions.
 * All endpoints are prefixed with "/api" and follow RESTful conventions.
 * Authentication is required for most operations except registration.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user accounts, profiles, and authentication")
public class UserRestController {

    private final IUserService userService;

    /**
     * Registers a new user account in the system.
     * Validates the provided user data for format and uniqueness before creating the account.
     * Upon successful registration, returns the created user profile with HTTP 201 status.
     *
     * @param userInsertDTO the user registration data containing credentials and personal info
     * @param bindingResult contains validation errors for the userInsertDTO
     * @return ResponseEntity containing the created UserReadOnlyDTO with HTTP 201 status
     *         and location header pointing to the new resource
     * @throws AppObjectAlreadyExistsException if username or email already exists in the system
     * @throws AppObjectInvalidArgumentException if registration data violates business rules
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with validated credentials and personal information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserReadOnlyDTO> registerUser(
            @Valid @RequestBody UserInsertDTO userInsertDTO,
            BindingResult bindingResult)
            throws AppObjectAlreadyExistsException, AppObjectInvalidArgumentException, ValidationException {

        log.info("USER REGISTRATION REQUEST - Username: {}, Email: {}, Name: {} {}",
                userInsertDTO.username(), userInsertDTO.email(),
                userInsertDTO.firstname(), userInsertDTO.lastname());

        if (bindingResult.hasErrors()) {
            log.warn("User registration failed - Validation errors for username: {}",
                    userInsertDTO.username());
            throw new ValidationException(bindingResult);
        }

        UserReadOnlyDTO registeredUser = userService.registerUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{username}")
                .buildAndExpand(registeredUser.username())
                .toUri();

        log.info("User registered successfully - ID: {}, Username: {}, Location: {}",
                registeredUser.id(), registeredUser.username(), location);

        return ResponseEntity.created(location).body(registeredUser);
    }

    /**
     * Retrieves the profile of a specific user by their username.
     * Returns public user information without sensitive data like password.
     * Requires that the authenticated user has appropriate permissions.
     *
     * @param username the unique username of the user to retrieve
     * @return ResponseEntity containing the UserReadOnlyDTO with user profile information
     * @throws AppObjectNotFoundException if the user with the specified username does not exist
     */
    @Operation(
            summary = "Get user profile by username",
            description = "Retrieves public profile information for a specific user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/{username}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUsername(
            @Parameter(description = "Username of the user to retrieve", required = true, example = "john_doe")
            @PathVariable @NotBlank(message = "Username is required") String username)
            throws AppObjectNotFoundException {

        log.debug("GET USER PROFILE REQUEST - Username: {}", username);

        UserReadOnlyDTO userProfile = userService.getUserByUsername(username);

        log.debug("User profile retrieved - Username: {}, Active: {}, Email: {}",
                userProfile.username(), userProfile.active(), userProfile.email());

        return ResponseEntity.ok(userProfile);
    }

    /**
     * Updates the profile information of an existing user.
     * Validates the updated data and ensures uniqueness of username and email.
     * Users can only update their own profile unless they have administrative privileges.
     *
     * @param username the current username of the user to update
     * @param userUpdateDTO the updated user data
     * @param bindingResult contains validation errors for the userUpdateDTO
     * @return ResponseEntity containing the updated UserReadOnlyDTO
     * @throws AppObjectNotFoundException if the user with the specified username does not exist
     * @throws AppObjectAlreadyExistsException if the new username or email conflicts with existing users
     * @throws AppObjectInvalidArgumentException if update data violates business rules
     */
    @Operation(
            summary = "Update user profile",
            description = "Updates profile information for an existing user, with validation for uniqueness."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "New username or email already exists"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this user")
    })
    @PutMapping("/{username}")
    public ResponseEntity<UserReadOnlyDTO> updateUser(
            @Parameter(description = "Current username of the user to update", required = true, example = "john_doe")
            @PathVariable @NotBlank(message = "Username is required") String username,
            @Valid @RequestBody UserInsertDTO userUpdateDTO,
            BindingResult bindingResult)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException, AppObjectInvalidArgumentException, ValidationException {

        log.info("UPDATE USER PROFILE REQUEST - Current Username: {}, New Username: {}, Email: {}",
                username, userUpdateDTO.username(), userUpdateDTO.email());

        if (bindingResult.hasErrors()) {
            log.warn("User update failed - Validation errors for current username: {}", username);
            throw new ValidationException(bindingResult);
        }

        UserReadOnlyDTO updatedUser = userService.updateUser(username, userUpdateDTO);

        log.info("User profile updated successfully - ID: {}, New Username: {}",
                updatedUser.id(), updatedUser.username());

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deactivates a user account (soft delete).
     * The user will no longer be able to log in but their data is preserved for historical purposes.
     * Typically restricted to administrators or the users themselves.
     *
     * @param username the username of the user to deactivate
     * @return ResponseEntity with HTTP 204 No Content status upon successful deactivation
     * @throws AppObjectNotFoundException if the user with the specified username does not exist
     */
    @Operation(
            summary = "Deactivate user account",
            description = "Soft deletes a user account, preventing login while preserving data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User account deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to deactivate this user")
    })
    @DeleteMapping("/{username}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "Username of the user to deactivate", required = true, example = "john_doe")
            @PathVariable @NotBlank(message = "Username is required") String username)
            throws AppObjectNotFoundException {

        log.info("DEACTIVATE USER REQUEST - Username: {}", username);

        userService.deactivateUser(username);

        log.info("User account deactivated successfully - Username: {}", username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deactivated user account.
     * Restores the user's ability to log in and access the system.
     * Typically restricted to administrators.
     *
     * @param username the username of the user to reactivate
     * @return ResponseEntity with HTTP 204 No Content status upon successful reactivation
     * @throws AppObjectNotFoundException if the user with the specified username does not exist
     */
    @Operation(
            summary = "Reactivate user account",
            description = "Restores a previously deactivated user account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User account reactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to reactivate this user")
    })
    @PutMapping("/{username}/reactivate")
    public ResponseEntity<Void> reactivateUser(
            @Parameter(description = "Username of the user to reactivate", required = true, example = "john_doe")
            @PathVariable @NotBlank(message = "Username is required") String username)
            throws AppObjectNotFoundException {

        log.info("REACTIVATE USER REQUEST - Username: {}", username);

        userService.reactivateUser(username);

        log.info("User account reactivated successfully - Username: {}", username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a paginated list of all users in the system.
     * Primarily intended for administrative purposes.
     * Results are ordered by username in ascending order.
     *
     * @param page the page number (0-based)
     * @param size the number of users per page, must be between 1 and 100
     * @return ResponseEntity containing a Page of UserReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    @Operation(
            summary = "Get paginated list of users",
            description = "Retrieves a paginated list of all users, ordered by username."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<Page<UserReadOnlyDTO>> getPaginatedUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
            throws AppObjectInvalidArgumentException {

        log.debug("GET PAGINATED USERS REQUEST - Page: {}, Size: {}", page, size);

        Page<UserReadOnlyDTO> usersPage = userService.getPaginatedUsers(page, size);

        log.debug("Users retrieved - Page: {}, Count: {}, Total Pages: {}",
                page, usersPage.getNumberOfElements(), usersPage.getTotalPages());

        return ResponseEntity.ok(usersPage);
    }
}