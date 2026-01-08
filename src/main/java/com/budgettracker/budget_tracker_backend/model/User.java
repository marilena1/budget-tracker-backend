package com.budgettracker.budget_tracker_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in the system.
 * Implements Spring Security's UserDetails for authentication and authorization.
 */
@Getter
@Setter
@Document(collection = "users")
public class User extends AbstractEntity implements UserDetails {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    @Indexed(unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$",
            message = "Password must contain at least 1 letter and 1 number")
    private String password;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Please provide a valid email address (e.g., user@example.com)")
    private String email;

    private boolean active = true;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstname;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastname;

    private Set<String> roleIds = new HashSet<>();

    /**
     * Adds a role to this user.
     * @param roleId the ID of the role to add
     */
    public void addRole(String roleId) {
        this.roleIds.add(roleId);
    }

    /**
     * Removes a role from this user.
     * @param roleId the ID of the role to remove
     */
    public void removeRole(String roleId) {
        this.roleIds.remove(roleId);
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return empty - authorities will be populated by UserDetailsService
        // The UserDetailsService will create a NEW UserDetails object with authorities
        return Collections.emptySet();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public  boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}
