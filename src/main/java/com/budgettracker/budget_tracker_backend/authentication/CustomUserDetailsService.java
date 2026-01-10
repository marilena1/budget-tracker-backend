package com.budgettracker.budget_tracker_backend.authentication;

import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.repository.UserRepository;
import com.budgettracker.budget_tracker_backend.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user details from the database and computes authorities using AuthorizationService.
 * This service bridges Spring Security authentication with the application's MongoDB data model.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    /**
     * Loads user details by username for Spring Security authentication.
     * This method is automatically called by Spring Security during login attempts.
     *
     * @param username the username identifying the user whose data is required
     * @return UserDetails object containing user credentials and authorities
     * @throws UsernameNotFoundException if user not found or account is disabled
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // TODO: Create UserService and use AppObjectNotFoundException instead
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User" + username + " not found."));

        // TODO: Use AppObjectNotAuthorizedException from UserService
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User account is disabled.");
        }

        Set<GrantedAuthority> authorities = authorizationService.deriveAuthorities(user);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
}
