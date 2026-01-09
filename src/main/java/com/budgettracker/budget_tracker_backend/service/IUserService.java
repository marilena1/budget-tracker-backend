package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.user.UserInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.user.UserReadOnlyDTO;
import org.springframework.data.domain.Page;

/**
 * Service interface for managing user accounts.
 * Defines business operations for user registration, profile management, and authentication.
 */
public interface IUserService {

    /**
     * Registers a new user account.
     * Validates uniqueness of username and email, encodes password, and assigns default roles.
     *
     * @param userInsertDTO the user registration data
     * @return UserReadOnlyDTO containing the created user without sensitive information
     * @throws AppObjectAlreadyExistsException if username or email already exists
     * @throws AppObjectInvalidArgumentException if registration data violates business rules
     */
    UserReadOnlyDTO registerUser(UserInsertDTO userInsertDTO)
            throws AppObjectAlreadyExistsException, AppObjectInvalidArgumentException;

    /**
     * Retrieves a user's profile by their unique username.
     * Returns public user information without sensitive data like password.
     *
     * @param username the username of the user to retrieve
     * @return UserReadOnlyDTO containing the user's public profile
     * @throws AppObjectNotFoundException if the user does not exist
     */
    UserReadOnlyDTO getUserByUsername(String username)
            throws AppObjectNotFoundException;

    /**
     * Updates a user's profile information.
     * Validates that the user exists and that new data doesn't conflict with existing users.
     *
     * @param username the username of the user to update
     * @param userUpdateDTO the updated user data (uses same structure as registration)
     * @return UserReadOnlyDTO containing the updated user profile
     * @throws AppObjectNotFoundException if the user does not exist
     * @throws AppObjectAlreadyExistsException if new username or email conflicts with existing users
     * @throws AppObjectInvalidArgumentException if update data violates business rules
     */
    UserReadOnlyDTO updateUser(String username, UserInsertDTO userUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException, AppObjectInvalidArgumentException;

    /**
     * Deactivates a user account (soft delete).
     * The user will no longer be able to log in but their data is preserved.
     *
     * @param username the username of the user to deactivate
     * @throws AppObjectNotFoundException if the user does not exist
     */
    void deactivateUser(String username)
            throws AppObjectNotFoundException;

    /**
     * Reactivates a previously deactivated user account.
     *
     * @param username the username of the user to reactivate
     * @throws AppObjectNotFoundException if the user does not exist
     */
    void reactivateUser(String username)
            throws AppObjectNotFoundException;

    /**
     * Retrieves a paginated list of all active users.
     * Useful for administrative purposes (though you may skip this for now).
     *
     * @param page the page number (0-based)
     * @param size the number of users per page, must be between 1 and 100
     * @return Page of UserReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    Page<UserReadOnlyDTO> getPaginatedUsers(int page, int size)
            throws AppObjectInvalidArgumentException;
}