package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.user.UserInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.user.UserReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * Service implementation for user management operations.
 * Contains business logic for user registration, profile management, and authentication.
 * Handles data validation, uniqueness checks, password encoding, and entity-DTO conversions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user account.
     * Validates uniqueness of username and email, encodes password, and assigns default roles.
     *
     * @param userInsertDTO the user registration data
     * @return UserReadOnlyDTO containing the created user without sensitive information
     * @throws AppObjectAlreadyExistsException if username or email already exists
     */
    @Override
    public UserReadOnlyDTO registerUser(UserInsertDTO userInsertDTO)
            throws AppObjectAlreadyExistsException {

        log.info("Registering new user with username: '{}' and email: '{}'",
                userInsertDTO.username(), userInsertDTO.email());

        // Validate username uniqueness
        if (userRepository.existsByUsername(userInsertDTO.username())) {
            log.warn("User registration failed: Username '{}' already exists", userInsertDTO.username());
            throw new AppObjectAlreadyExistsException("User", userInsertDTO.username());
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(userInsertDTO.email())) {
            log.warn("User registration failed: Email '{}' already registered", userInsertDTO.email());
            throw new AppObjectAlreadyExistsException("User", userInsertDTO.email());
        }

        // Create and populate user entity
        User user = new User();
        user.setUsername(userInsertDTO.username());
        user.setEmail(userInsertDTO.email());
        user.setPassword(passwordEncoder.encode(userInsertDTO.password()));
        user.setFirstname(userInsertDTO.firstname());
        user.setLastname(userInsertDTO.lastname());
        user.setActive(true);
        user.setRoleIds(new HashSet<>()); // Default empty roles

        log.debug("Saving new user: '{}'", userInsertDTO.username());
        User savedUser = userRepository.save(user);

        log.info("User registered successfully. ID: {}, Username: '{}'",
                savedUser.getId(), savedUser.getUsername());

        return UserReadOnlyDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstname(savedUser.getFirstname())
                .lastname(savedUser.getLastname())
                .active(savedUser.isActive())
                .roleNames(new HashSet<>()) // Empty for now
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    /**
     * Retrieves a user's profile by their unique username.
     * Returns public user information without sensitive data like password.
     *
     * @param username the username of the user to retrieve
     * @return UserReadOnlyDTO containing the user's public profile
     * @throws AppObjectNotFoundException if the user does not exist
     */
    @Override
    public UserReadOnlyDTO getUserByUsername(String username)
            throws AppObjectNotFoundException {

        log.debug("Retrieving user profile for username: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: '{}'", username);
                    return new AppObjectNotFoundException("User", username);
                });

        log.debug("User profile retrieved: ID: {}, Username: '{}'", user.getId(), username);

        return UserReadOnlyDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .active(user.isActive())
                .roleNames(new HashSet<>()) // Empty for now
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Updates a user's profile information.
     * Validates that the user exists and that new data doesn't conflict with existing users.
     *
     * @param username the username of the user to update
     * @param userUpdateDTO the updated user data
     * @return UserReadOnlyDTO containing the updated user profile
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectAlreadyExistsException if new username or email conflicts with existing users
     */
    @Override
    public UserReadOnlyDTO updateUser(String username, UserInsertDTO userUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException {

        log.info("Updating user profile for username: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for update: '{}'", username);
                    return new AppObjectNotFoundException("User", username);
                });

        // Check if username changed and conflicts with another user
        if (!user.getUsername().equals(userUpdateDTO.username()) &&
                userRepository.existsByUsername(userUpdateDTO.username())) {
            log.warn("User update failed: New username '{}' already exists", userUpdateDTO.username());
            throw new AppObjectAlreadyExistsException("User", userUpdateDTO.username());
        }

        // Check if email changed and conflicts with another user
        if (!user.getEmail().equals(userUpdateDTO.email()) &&
                userRepository.existsByEmail(userUpdateDTO.email())) {
            log.warn("User update failed: New email '{}' already registered", userUpdateDTO.email());
            throw new AppObjectAlreadyExistsException("User", userUpdateDTO.email());
        }

        // Update user fields
        user.setUsername(userUpdateDTO.username());
        user.setEmail(userUpdateDTO.email());
        user.setFirstname(userUpdateDTO.firstname());
        user.setLastname(userUpdateDTO.lastname());

        // Update password only if provided (not null or empty)
        if (userUpdateDTO.password() != null && !userUpdateDTO.password().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateDTO.password()));
            log.debug("Password updated for user: '{}'", username);
        }

        log.debug("Saving updated user: '{}'", username);
        User updatedUser = userRepository.save(user);

        log.info("User profile updated successfully. ID: {}, Username: '{}'",
                updatedUser.getId(), updatedUser.getUsername());

        return UserReadOnlyDTO.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .firstname(updatedUser.getFirstname())
                .lastname(updatedUser.getLastname())
                .active(updatedUser.isActive())
                .roleNames(new HashSet<>()) // Empty for now
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }

    /**
     * Deactivates a user account (soft delete).
     * The user will no longer be able to log in but their data is preserved.
     *
     * @param username the username of the user to deactivate
     * @throws AppObjectNotFoundException if the user does not exist
     */
    @Override
    public void deactivateUser(String username)
            throws AppObjectNotFoundException {

        log.info("Deactivating user account: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for deactivation: '{}'", username);
                    return new AppObjectNotFoundException("User", username);
                });

        if (!user.isActive()) {
            log.warn("User '{}' is already deactivated", username);
            return;
        }

        user.setActive(false);
        userRepository.save(user);

        log.info("User account deactivated: '{}'", username);
    }

    /**
     * Reactivates a previously deactivated user account.
     *
     * @param username the username of the user to reactivate
     * @throws AppObjectNotFoundException if the user does not exist
     */
    @Override
    public void reactivateUser(String username)
            throws AppObjectNotFoundException {

        log.info("Reactivating user account: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for reactivation: '{}'", username);
                    return new AppObjectNotFoundException("User", username);
                });

        if (user.isActive()) {
            log.warn("User '{}' is already active", username);
            return;
        }

        user.setActive(true);
        userRepository.save(user);

        log.info("User account reactivated: '{}'", username);
    }

    /**
     * Retrieves a paginated list of all active users.
     * Useful for administrative purposes.
     *
     * @param page the page number (0-based)
     * @param size the number of users per page, must be between 1 and 100
     * @return Page of UserReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    @Override
    public Page<UserReadOnlyDTO> getPaginatedUsers(int page, int size)
            throws AppObjectInvalidArgumentException {

        log.debug("Retrieving paginated users, page={}, size={}", page, size);

        if (page < 0) {
            log.warn("Invalid page number {} for user pagination", page);
            throw new AppObjectInvalidArgumentException("page", "must be zero or positive");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size {} for user pagination", size);
            throw new AppObjectInvalidArgumentException("size", "must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<User> usersPage = userRepository.findAll(pageable);

        log.debug("Returning {} users, page {} of {}",
                usersPage.getNumberOfElements(), page, usersPage.getTotalPages());

        return usersPage.map(user -> UserReadOnlyDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .active(user.isActive())
                .roleNames(new HashSet<>()) // Empty for now
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build());
    }
}