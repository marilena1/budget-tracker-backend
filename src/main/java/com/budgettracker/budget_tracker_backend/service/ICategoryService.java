package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryReadOnlyDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for managing transaction categories.
 * Defines business operations for category CRUD and retrieval.
 */
public interface ICategoryService {

    /**
     * Creates a new transaction category.
     * Validates that category name is unique.
     *
     * @param categoryInsertDTO the category data to create
     * @return CategoryReadOnlyDTO containing the created category
     * @throws AppObjectAlreadyExistsException if a category with the same name already exists
     */
    CategoryReadOnlyDTO createCategory(CategoryInsertDTO categoryInsertDTO)
            throws AppObjectAlreadyExistsException;

    /**
     * Updates an existing category.
     * Validates that the category exists and new name doesn't conflict with existing categories.
     *
     * @param categoryId the ID of the category to update
     * @param categoryUpdateDTO the updated category data (uses same structure as creation)
     * @return CategoryReadOnlyDTO containing the updated category
     * @throws AppObjectNotFoundException if the category does not exist
     * @throws AppObjectAlreadyExistsException if new category name conflicts with existing category
     */
    CategoryReadOnlyDTO updateCategory(String categoryId, CategoryInsertDTO categoryUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException;

    /**
     * Deletes a category.
     * Validates that the category exists and is not used by any transactions.
     *
     * @param categoryId the ID of the category to delete
     * @throws AppObjectNotFoundException if the category does not exist
     * @throws AppObjectInvalidArgumentException if category is used by transactions
     */
    void deleteCategory(String categoryId)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException;


    /**
     * Retrieves a category by its unique identifier.
     *
     * @param categoryId the ID of the category to retrieve
     * @return CategoryReadOnlyDTO containing the category details
     * @throws AppObjectNotFoundException if the category does not exist
     */
    CategoryReadOnlyDTO getCategoryById(String categoryId)
            throws AppObjectNotFoundException;

    /**
     * Retrieves a category by its name.
     *
     * @param categoryName the name of the category to retrieve
     * @return CategoryReadOnlyDTO containing the category details
     * @throws AppObjectNotFoundException if the category does not exist
     */
    CategoryReadOnlyDTO getCategoryByName(String categoryName)
            throws AppObjectNotFoundException;

    /**
     * Retrieves all categories.
     *
     * @return List of all CategoryReadOnlyDTOs
     */
    List<CategoryReadOnlyDTO> getAllCategories();

    /**
     * Retrieves a paginated list of categories.
     *
     * @param page the page number (0-based)
     * @param size the number of categories per page, must be between 1 and 100
     * @return Page of CategoryReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    Page<CategoryReadOnlyDTO> getPaginatedCategories(int page, int size)
            throws AppObjectInvalidArgumentException;

    /**
     * Searches categories by name (case-insensitive contains search).
     *
     * @param nameQuery the search query for category name
     * @return List of CategoryReadOnlyDTOs matching the search
     */
    List<CategoryReadOnlyDTO> searchCategoriesByName(String nameQuery);
}