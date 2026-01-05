package com.budgettracker.budget_tracker_backend.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
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

    @Indexed(unique = true)
    private String username;

    private String email;

    private String password;

    private Set<String> roleIds = new HashSet<>();

    private Set<String> roleNames = new HashSet<>();
    private Set<String> capabilityNames = new HashSet<>();

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
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        roleNames.forEach(roleName ->
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleName))
        );

        capabilityNames.forEach(capability ->
                grantedAuthorities.add(new SimpleGrantedAuthority(capability))
        );

        return grantedAuthorities;
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
        return true;
    }
}
