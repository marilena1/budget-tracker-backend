package com.budgettracker.budget_tracker_backend.authentication;

import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.repository.UserRepository;
import com.budgettracker.budget_tracker_backend.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found: {}", username);
                    return new UsernameNotFoundException("Invalid credentials");
                });

        if (!user.isEnabled()) {
            log.warn("Authentication failed - disabled account: {}", username);
            throw new UsernameNotFoundException("Account disabled");
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
