package com.budgettracker.budget_tracker_backend.authentication;

import com.budgettracker.budget_tracker_backend.dto.auth.AuthenticationRequestDTO;
import com.budgettracker.budget_tracker_backend.dto.auth.AuthenticationResponseDTO;
import com.budgettracker.budget_tracker_backend.model.User;
import com.budgettracker.budget_tracker_backend.repository.UserRepository;
import com.budgettracker.budget_tracker_backend.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service responsible for user authentication and JWT token generation.
 * Integrates with Spring Security's AuthenticationManager for credential validation
 * and uses AuthorizationService to resolve user roles from MongoDB references.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationService authorizationService;

    /**
     * Authenticates a user with username and password credentials.
     * Uses Spring Security's AuthenticationManager for validation and generates
     * a JWT token containing the user's primary role for authorization.
     * The role is resolved from MongoDB document references via AuthorizationService.
     *
     * @param dto Authentication request containing username and password
     * @return AuthenticationResponseDTO containing user info and JWT token
     * @throws BadCredentialsException if authentication fails
     */
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {
        log.debug("Authentication attempt for user: {}", dto.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        log.debug("Authentication successful for user: {}", dto.username());

        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> {
                    log.error("Data inconsistency: User '{}' authenticated but not found", dto.username());
                    return new IllegalStateException("Authentication data inconsistency");
                });

        String userRole = getPrimaryRoleName(user);
        log.debug("Resolved primary role '{}' for user: {}", userRole, user.getUsername());

        String token = jwtService.generateToken(user.getUsername(), userRole);
        log.info("Successful authentication for user: {} with role: {}", user.getUsername(), userRole);

        return AuthenticationResponseDTO.builder()
                .firstname(user.getFirstname() != null ? user.getFirstname() : "")
                .lastname(user.getLastname() != null ? user.getLastname() : "")
                .token(token)
                .build();
    }

    /**
     * Gets the primary role name for JWT token.
     * Uses AuthorizationService to get role names and picks first one.
     */
    private String getPrimaryRoleName(User user) {
        log.debug("No roles assigned to user '{}', using default 'USER' role", user.getUsername());
        Set<String> roleNames = authorizationService.getRoleNames(user);
        if (roleNames.isEmpty()) {
            return "USER";
        }
        return roleNames.iterator().next();
    }
}