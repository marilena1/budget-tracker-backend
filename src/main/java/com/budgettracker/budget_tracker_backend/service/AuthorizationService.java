package com.budgettracker.budget_tracker_backend.service;

import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.model.auth.Role;
import com.budgettracker.budget_tracker_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for computing user permissions and authorities.
 * Translates database relationships (User → roleIds → Role → capabilityNames)
 * into Spring Security GrantedAuthority objects for authorization checks.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final RoleRepository roleRepository;

    /**
     * Derives Spring Security authorities from a user's assigned roles.
     * Combines role-based authorities (prefixed with "ROLE_") with
     * capability-based authorities for fine-grained permission checking.
     *
     * @param user the user entity to compute authorities for
     * @return set of GrantedAuthority objects ready for Spring Security
     */
    public Set<GrantedAuthority> deriveAuthorities(User user) {
        if (user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
            return Collections.emptySet();
        }

        List<Role> userRoles = roleRepository.findAllById(user.getRoleIds());
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : userRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            if (role.getCapabilityNames() != null) {
                for (String capabilityName : role.getCapabilityNames()) {
                    authorities.add(new SimpleGrantedAuthority(capabilityName));
                }
            }
        }
        return authorities;
    }

    /**
     * Retrieves the display names of all roles assigned to a user.
     *
     * @param user the user entity
     * @return set of role names (e.g., "USER", "ADMIN")
     */
    public Set<String> getRoleNames(User user) {
        return roleRepository.findAllById(user.getRoleIds())
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all capability names (permissions) assigned to a user
     * through their roles.
     *
     * @param user the user entity
     * @return set of capability names (e.g., "transaction:create", "category:read")
     */
    public Set<String> getCapabilityNames(User user) {
        if (user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> allCapabilities = new HashSet<>();
        roleRepository.findAllById(user.getRoleIds())
                .forEach(role -> {
                    if (role.getCapabilityNames() != null) {
                        allCapabilities.addAll(role.getCapabilityNames());
                    }
                });
        return allCapabilities;
    }

    /**
     * Checks if a user has a specific role.
     *
     * @param user the user entity
     * @param roleName the role name to check (e.g., "USER", "ADMIN")
     * @return true if the user has the specified role
     */
    public boolean hasRole(User user, String roleName) {
        return getRoleNames(user).contains(roleName);
    }

    /**
     * Checks if a user has a specific capability/permission.
     *
     * @param user the user entity
     * @param capabilityName the capability to check (e.g., "transaction:create")
     * @return true if the user has the specified capability
     */
    public boolean hasCapability(User user, String capabilityName) {
            return getCapabilityNames(user).contains(capabilityName);
    }

    /**
     * Checks if a user has any of the specified roles.
     *
     * @param user the user entity
     * @param roleNames variable array of role names to check
     * @return true if the user has at least one of the specified roles
     */
    public boolean hasAnyRole(User user, String... roleNames) {
        Set<String> userRoles = getRoleNames(user);
        for (String roleName : roleNames) {
            if (userRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a user has any of the specified capabilities.
     *
     * @param user the user entity
     * @param capabilityNames variable array of capability names to check
     * @return true if the user has at least one of the specified capabilities
     */
    public boolean hasAnyCapabilities(User user, String... capabilityNames) {
        Set<String> userCapabilities = getCapabilityNames(user);
        for (String capabilityName : capabilityNames) {
            if (userCapabilities.contains(capabilityName)) {
                return true;
            }
        }
        return false;
    }
}