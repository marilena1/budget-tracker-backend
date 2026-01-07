package com.budgettracker.budget_tracker_backend.config;

import com.budgettracker.budget_tracker_backend.model.auth.Role;
import com.budgettracker.budget_tracker_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Database seeder that runs on application startup to populate initial data.
 * Ensures essential system roles exist before the application becomes operational.
 * Implements CommandLineRunner to execute after Spring context is loaded.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    /**
     * Executes database seeding logic when the application starts.
     * Checks if roles already exist to prevent duplicate seeding on subsequent startups.
     *
     * @param args command line arguments (unused)
     */
    @Override
    public void run(String... args) {
        // Safety check: Only seed if database is empty
        if (roleRepository.count() > 0) {
            return;  // Skip if roles already exist
        }

        Role userRole = Role.builder()
                .name("USER")
                .description("Standard user with basic permissions")
                .capabilityNames(Set.of(
                        "CREATE_TRANSACTION",
                        "VIEW_OWN_TRANSACTIONS",
                        "VIEW_CATEGORIES"
                ))
                .build();

        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator with full system access")
                .capabilityNames(Set.of(
                        "CREATE_TRANSACTION",
                        "VIEW_ALL_TRANSACTIONS",
                        "DELETE_TRANSACTIONS",
                        "MANAGE_USERS",
                        "MANAGE_CATEGORIES",
                        "MANAGE_ROLES"
                ))
                .build();

        roleRepository.saveAll(Set.of(userRole, adminRole));
    }
}