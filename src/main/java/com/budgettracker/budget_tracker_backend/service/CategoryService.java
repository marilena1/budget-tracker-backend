package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.model.Category;
import com.budgettracker.budget_tracker_backend.repository.CategoryRepository;
import com.budgettracker.budget_tracker_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for category management operations.
 * Contains business logic for creating, reading, updating, and deleting transaction categories.
 * Handles data validation, uniqueness checks, and entity-DTO conversions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new transaction category.
     * Validates category name uniqueness and persists the category with provided details.
     *
     * @param categoryInsertDTO the category data provided by the user
     * @return CategoryReadOnlyDTO containing the created category with system-generated fields
     * @throws AppObjectAlreadyExistsException if a category with the same name already exists
     */
    @Override
    public CategoryReadOnlyDTO createCategory(CategoryInsertDTO categoryInsertDTO)
            throws AppObjectAlreadyExistsException {

        log.info("Creating new category with name: '{}'", categoryInsertDTO.name());

        // Validate category name uniqueness
        if (categoryRepository.existsByName(categoryInsertDTO.name())) {
            log.warn("Category creation failed: Name '{}' already exists", categoryInsertDTO.name());
            throw new AppObjectAlreadyExistsException("Category", categoryInsertDTO.name());
        }

        Category category = new Category();
        category.setName(categoryInsertDTO.name());
        category.setDescription(categoryInsertDTO.description());
        category.setColor(categoryInsertDTO.color());

        log.debug("Saving new category: '{}'", categoryInsertDTO.name());

        Category saved = categoryRepository.save(category);

        log.info("Category created successfully. ID: {}, Name: '{}'",
                saved.getId(), saved.getName());

        return CategoryReadOnlyDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .color(saved.getColor())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing category with new data.
     * Validates category existence, name uniqueness (if changed), and updates all fields.
     *
     * @param categoryId the unique identifier of the category to update
     * @param categoryUpdateDTO the new category data
     * @return CategoryReadOnlyDTO containing the updated category
     * @throws AppObjectNotFoundException if the category does not exist
     * @throws AppObjectAlreadyExistsException if new category name conflicts with existing category
     */
    @Override
    public CategoryReadOnlyDTO updateCategory(String categoryId, CategoryInsertDTO categoryUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException {

        log.info("Updating category ID: {}", categoryId);

        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found for update: ID: {}", categoryId);
                    return new AppObjectNotFoundException("Category", categoryId);
                });

        if (!existingCategory.getName().equals(categoryUpdateDTO.name()) &&
                categoryRepository.existsByNameAndIdNot(categoryUpdateDTO.name(), categoryId)) {
            log.warn("Category update failed: Name '{}' already exists", categoryUpdateDTO.name());
            throw new AppObjectAlreadyExistsException("Category", categoryUpdateDTO.name());
        }

        existingCategory.setName(categoryUpdateDTO.name());
        existingCategory.setDescription(categoryUpdateDTO.description());
        existingCategory.setColor(categoryUpdateDTO.color());

        log.debug("Saving updated category: ID: {}", categoryId);
        Category updatedCategory = categoryRepository.save(existingCategory);

        log.info("Category updated successfully. ID: {}, Name: '{}'",
                categoryId, updatedCategory.getName());

        return CategoryReadOnlyDTO.builder()
                .id(updatedCategory.getId())
                .name(updatedCategory.getName())
                .description(updatedCategory.getDescription())
                .color(updatedCategory.getColor())
                .createdAt(updatedCategory.getCreatedAt())
                .updatedAt(updatedCategory.getUpdatedAt())
                .build();
    }

    /**
     * Deletes a category.
     * Validates that the category exists and is not used by any transactions.
     *
     * @param categoryId the ID of the category to delete
     * @throws AppObjectNotFoundException if the category does not exist
     * @throws AppObjectInvalidArgumentException if category is used by transactions
     */
    @Override
    public void deleteCategory(String categoryId)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.info("Deleting category ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found for deletion: ID: {}", categoryId);
                    return new AppObjectNotFoundException("Category", categoryId);
                });

        if (transactionRepository.existsByCategoryId(categoryId)) {
            log.warn("Category deletion failed: Category '{}' is used by transactions",
                    category.getName());
            throw new AppObjectInvalidArgumentException("category",
                    "Cannot delete category '" + category.getName() +
                            "' because it is used by transactions. " +
                            "Please reassign or delete the transactions first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully. ID: {}, Name: '{}'",
                categoryId, category.getName());
    }

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param categoryId the ID of the category to retrieve
     * @return CategoryReadOnlyDTO containing the category details
     * @throws AppObjectNotFoundException if the category does not exist
     */
    @Override
    public CategoryReadOnlyDTO getCategoryById(String categoryId)
            throws AppObjectNotFoundException {

        log.debug("Retrieving category by ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", categoryId);
                    return new AppObjectNotFoundException("Category", categoryId);
                });

        log.debug("Category retrieved: ID: {}, Name: '{}'", categoryId, category.getName());

        return CategoryReadOnlyDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Retrieves a category by its exact name.
     *
     * @param categoryName the name of the category to retrieve
     * @return CategoryReadOnlyDTO containing the category details
     * @throws AppObjectNotFoundException if the category does not exist
     */
    @Override
    public CategoryReadOnlyDTO getCategoryByName(String categoryName)
            throws AppObjectNotFoundException {

        log.debug("Retrieving category by name: '{}'", categoryName);

        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> {
                    log.warn("Category not found with name: '{}'", categoryName);
                    return new AppObjectNotFoundException("Category", categoryName);
                });

        log.debug("Category retrieved: Name: '{}', ID: {}", categoryName, category.getId());

        return CategoryReadOnlyDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Retrieves all categories in the system.
     *
     * @return List of all CategoryReadOnlyDTOs
     */
    @Override
    public List<CategoryReadOnlyDTO> getAllCategories() {

        log.debug("Retrieving all categories");

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        log.debug("Retrieved {} categories", categories.size());

        return categories.stream()
                .map(category -> CategoryReadOnlyDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .color(category.getColor())
                        .createdAt(category.getCreatedAt())
                        .updatedAt(category.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a paginated list of categories.
     * Results are ordered by name ascending.
     * Includes validation for pagination parameters to ensure valid database queries.
     *
     * @param page the page number (0-based)
     * @param size the number of categories per page, must be between 1 and 100
     * @return Page of CategoryReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if page is negative or size is not between 1 and 100
     */
    @Override
    public Page<CategoryReadOnlyDTO> getPaginatedCategories(int page, int size)
            throws AppObjectInvalidArgumentException {

        log.debug("Getting paginated categories, page={}, size={}", page, size);

        if (page < 0) {
            log.warn("Invalid page number {} for category pagination", page);
            throw new AppObjectInvalidArgumentException("page", "must be zero or positive");
        }
        if (size <= 0 || size > 100) {
            log.warn("Invalid page size {} for category pagination", size);
            throw new AppObjectInvalidArgumentException("size", "must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categoriesPage = categoryRepository.findAll(pageable);

        log.debug("Returning {} categories, page {} of {}",
                categoriesPage.getNumberOfElements(), page, categoriesPage.getTotalPages());

        return categoriesPage.map(category -> CategoryReadOnlyDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build());
    }

    /**
     * Searches categories by name using case-insensitive contains matching.
     * Returns all categories if the search query is null or empty.
     * Useful for autocomplete or filtering category lists.
     *
     * @param nameQuery the search query for category name. If null or empty,
     *                  returns all categories sorted by name.
     * @return List of CategoryReadOnlyDTOs matching the search, or all categories
     *         if no search query is provided.
     */
    @Override
    public List<CategoryReadOnlyDTO> searchCategoriesByName(String nameQuery) {

        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            log.debug("Empty search query, returning all categories");
            // Option: return all categories or empty list
            return getAllCategories();
        }

        log.debug("Searching categories by name query: '{}'", nameQuery);

        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(nameQuery);

        log.debug("Found {} categories matching query '{}'", categories.size(), nameQuery);

        return categories.stream()
                .map(category -> CategoryReadOnlyDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .color(category.getColor())
                        .createdAt(category.getCreatedAt())
                        .updatedAt(category.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}