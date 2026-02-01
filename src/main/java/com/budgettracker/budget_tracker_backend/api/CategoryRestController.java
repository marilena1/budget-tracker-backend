package com.budgettracker.budget_tracker_backend.api;

import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectAlreadyExistsException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectInvalidArgumentException;
import com.budgettracker.budget_tracker_backend.core.exceptions.AppObjectNotFoundException;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryInsertDTO;
import com.budgettracker.budget_tracker_backend.dto.category.CategoryReadOnlyDTO;
import com.budgettracker.budget_tracker_backend.service.ICategoryService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller for managing transaction category operations.
 * Provides endpoints for CRUD operations, listing, and searching categories.
 * All endpoints are prefixed with "/api" and require appropriate authentication.
 * Categories are used to organize and classify financial transactions.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing transaction categories")
public class CategoryRestController {

    private final ICategoryService categoryService;

    /**
     * Creates a new transaction category.
     * Validates that the category name is unique and all required fields are provided.
     * Returns the created category with a location header containing its URI.
     *
     * @param categoryInsertDTO the category data to create, validated for constraints
     * @param bindingResult contains validation errors for the categoryInsertDTO
     * @return ResponseEntity containing the created CategoryReadOnlyDTO with HTTP 201 status
     *         and location header pointing to the new resource
     * @throws AppObjectAlreadyExistsException if a category with the same name already exists
     */
    @Operation(
            summary = "Create a new category",
            description = "Creates a new transaction category with validated data. Category names must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    @PostMapping
    public ResponseEntity<CategoryReadOnlyDTO> createCategory(
            @Valid @RequestBody CategoryInsertDTO categoryInsertDTO)
            throws AppObjectAlreadyExistsException {

        log.info("CREATE CATEGORY REQUEST - Name: '{}', Description length: {}",
                categoryInsertDTO.name(),
                categoryInsertDTO.description() != null ? categoryInsertDTO.description().length() : 0);

        CategoryReadOnlyDTO createdCategory = categoryService.createCategory(categoryInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.id())
                .toUri();

        log.info("Category created successfully - ID: {}, Name: '{}', Location: {}",
                createdCategory.id(), createdCategory.name(), location);

        return ResponseEntity.created(location).body(createdCategory);
    }

    /**
     * Updates an existing category with new data.
     * Validates that the category exists and that the new name doesn't conflict with existing categories.
     * All category fields can be updated in a single operation.
     *
     * @param categoryId the unique identifier of the category to update
     * @param categoryUpdateDTO the updated category data to apply
     * @param bindingResult contains validation errors for the categoryUpdateDTO
     * @return ResponseEntity containing the updated CategoryReadOnlyDTO
     * @throws AppObjectNotFoundException if the category with the given ID does not exist
     * @throws AppObjectAlreadyExistsException if the new category name conflicts with an existing category
     */
    @Operation(
            summary = "Update a category",
            description = "Updates an existing category with new data. Validates name uniqueness if changed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "New category name already exists")
    })
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryReadOnlyDTO> updateCategory(
            @Parameter(description = "ID of the category to update", required = true, example = "cat_123")
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId,
            @Valid @RequestBody CategoryInsertDTO categoryUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExistsException {

        log.info("UPDATE CATEGORY REQUEST - ID: {}, New Name: '{}'",
                categoryId, categoryUpdateDTO.name());

        CategoryReadOnlyDTO updatedCategory = categoryService.updateCategory(categoryId, categoryUpdateDTO);

        log.info("Category updated successfully - ID: {}, Name: '{}'",
                categoryId, updatedCategory.name());

        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Deletes a specific category.
     * Validates that the category exists and is not being used by any transactions.
     * Returns HTTP 204 No Content upon successful deletion.
     *
     * @param categoryId the ID of the category to delete
     * @return ResponseEntity with HTTP 204 No Content status upon successful deletion
     * @throws AppObjectNotFoundException if the category does not exist
     * @throws AppObjectInvalidArgumentException if the category is used by transactions
     */
    @Operation(
            summary = "Delete a category",
            description = "Deletes a category if it is not used by any transactions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Category is used by transactions")
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete", required = true, example = "cat_123")
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId)
            throws AppObjectNotFoundException, AppObjectInvalidArgumentException {

        log.info("DELETE CATEGORY REQUEST - ID: {}", categoryId);

        categoryService.deleteCategory(categoryId);

        log.info("Category deleted successfully - ID: {}", categoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a specific category by its unique identifier.
     * Returns complete category information including metadata.
     *
     * @param categoryId the ID of the category to retrieve
     * @return ResponseEntity containing the CategoryReadOnlyDTO
     * @throws AppObjectNotFoundException if the category with the given ID does not exist
     */
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves complete category information by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryReadOnlyDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve", required = true, example = "cat_123")
            @PathVariable @NotBlank(message = "Category ID is required") String categoryId)
            throws AppObjectNotFoundException {

        log.debug("GET CATEGORY BY ID REQUEST - ID: {}", categoryId);

        CategoryReadOnlyDTO category = categoryService.getCategoryById(categoryId);

        log.debug("Category retrieved - ID: {}, Name: '{}'",
                category.id(), category.name());

        return ResponseEntity.ok(category);
    }

    /**
     * Retrieves a specific category by its exact name.
     * Useful for looking up categories when only the name is known.
     *
     * @param categoryName the exact name of the category to retrieve
     * @return ResponseEntity containing the CategoryReadOnlyDTO
     * @throws AppObjectNotFoundException if the category with the given name does not exist
     */
    @Operation(
            summary = "Get category by name",
            description = "Retrieves category information by its exact name (case-sensitive)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/name/{categoryName}")
    public ResponseEntity<CategoryReadOnlyDTO> getCategoryByName(
            @Parameter(description = "Exact name of the category to retrieve", required = true, example = "Groceries")
            @PathVariable @NotBlank(message = "Category name is required") String categoryName)
            throws AppObjectNotFoundException {

        log.debug("GET CATEGORY BY NAME REQUEST - Name: '{}'", categoryName);

        CategoryReadOnlyDTO category = categoryService.getCategoryByName(categoryName);

        log.debug("Category retrieved - Name: '{}', ID: {}",
                category.name(), category.id());

        return ResponseEntity.ok(category);
    }

    /**
     * Retrieves all categories in the system.
     * Returns a complete list of categories ordered alphabetically by name.
     * Useful for populating dropdowns or category selection interfaces.
     *
     * @return ResponseEntity containing a List of all CategoryReadOnlyDTOs
     */
    @Operation(
            summary = "Get all categories",
            description = "Retrieves all categories, ordered alphabetically by name."
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping
    public ResponseEntity<List<CategoryReadOnlyDTO>> getAllCategories() {

        log.debug("GET ALL CATEGORIES REQUEST");

        List<CategoryReadOnlyDTO> categories = categoryService.getAllCategories();

        log.debug("All categories retrieved - Count: {}", categories.size());

        return ResponseEntity.ok(categories);
    }

    /**
     * Retrieves a paginated list of categories.
     * Results are ordered by name in ascending order.
     * Supports efficient browsing of large category collections.
     *
     * @param page the page number (0-based)
     * @param size the number of categories per page, must be between 1 and 100
     * @return ResponseEntity containing a Page of CategoryReadOnlyDTOs
     * @throws AppObjectInvalidArgumentException if pagination parameters are invalid
     */
    @Operation(
            summary = "Get paginated categories",
            description = "Retrieves categories in paginated format, ordered by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/paginated")
    public ResponseEntity<Page<CategoryReadOnlyDTO>> getPaginatedCategories(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100") int size)
            throws AppObjectInvalidArgumentException {

        log.debug("GET PAGINATED CATEGORIES REQUEST - Page: {}, Size: {}", page, size);

        Page<CategoryReadOnlyDTO> categoriesPage = categoryService.getPaginatedCategories(page, size);

        log.debug("Paginated categories retrieved - Page: {}, Count: {}, Total Pages: {}",
                page, categoriesPage.getNumberOfElements(), categoriesPage.getTotalPages());

        return ResponseEntity.ok(categoriesPage);
    }

    /**
     * Searches categories by name using case-insensitive contains matching.
     * Returns categories whose names contain the search query.
     * Useful for autocomplete functionality or filtering category lists.
     *
     * @param query the search query string (optional, returns all categories if empty)
     * @return ResponseEntity containing a List of matching CategoryReadOnlyDTOs
     */
    @Operation(
            summary = "Search categories by name",
            description = "Searches categories by name using case-insensitive contains matching."
    )
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @GetMapping("/search")
    public ResponseEntity<List<CategoryReadOnlyDTO>> searchCategoriesByName(
            @Parameter(description = "Search query for category name", example = "food")
            @RequestParam(required = false) String query) {

        log.debug("SEARCH CATEGORIES REQUEST - Query: '{}'", query);

        List<CategoryReadOnlyDTO> categories = categoryService.searchCategoriesByName(query);

        log.debug("Category search completed - Query: '{}', Results: {}",
                query, categories.size());

        return ResponseEntity.ok(categories);
    }

}