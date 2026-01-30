package com.budgettracker.budget_tracker_backend.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Main security configuration class for the Budget Tracker application.
 * Configures JWT-based authentication, authorization rules, CORS, and exception handling.
 * Implements stateless session management with custom JSON responses for security events.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the main security filter chain for HTTP requests.
     * Sets up public endpoints, authentication requirements, JWT validation,
     * and custom exception handling for authentication and authorization failures.
     *
     * @param http the HttpSecurity to configure
     * @param authenticationProvider the authentication provider for username/password validation
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {
        log.info("Configuring Spring Security filter chain");
        http
                .cors(cors -> {
                    log.debug("Configuring CORS for frontend origins");
                    cors.configurationSource(corsConfigurationSource());
                })
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/authenticate").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/configuration/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").authenticated()
                        .requestMatchers("/api/transactions/**").authenticated()
                        .requestMatchers("/api/categories/**").authenticated()


                        // Example role-based protection (adjust based on your needs)
                        // .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/api/budgets/**").hasAnyRole("USER", "ADMIN")
                        // .requestMatchers(HttpMethod.GET, "/api/reports/**").hasAuthority("report:read")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> {
                    log.debug("Configuring stateless session management");
                    session.sessionCreationPolicy(STATELESS);
                })
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    log.debug("Configuring custom exception handlers");
                    exception
                            .authenticationEntryPoint(customAuthenticationEntryPoint())
                            .accessDeniedHandler(customAccessDeniedHandler());
                });

        log.info("Spring Security configuration completed successfully");
        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * Allows requests from common frontend development origins and exposes
     * the Authorization header for JWT token usage in frontend applications.
     *
     * @return CorsConfigurationSource with allowed origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Creating CORS configuration for frontend origins: localhost:3000, localhost:4200");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React
                "http://localhost:4200",  // Angular
                "http://localhost:8080"   // Your backend (if needed)
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization")); // Expose Authorization header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured with allowed origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    /**
     * Configures the authentication provider for username/password authentication.
     * Uses DaoAuthenticationProvider with the custom UserDetailsService and BCrypt password encoder.
     *
     * @param userDetailsService the service to load user details by username
     * @param passwordEncoder the password encoder for credential validation
     * @return configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        log.debug("Creating DaoAuthenticationProvider with UserDetailsService and BCrypt encoder");

        // Updated to non-deprecated constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        log.info("Authentication provider configured successfully");
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Spring bean for use in authentication services.
     *
     * @param config the AuthenticationConfiguration
     * @return the AuthenticationManager
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Creating AuthenticationManager bean");
        AuthenticationManager authenticationManager = config.getAuthenticationManager();
        log.info("AuthenticationManager bean created successfully");
        return authenticationManager;
    }

    /**
     * Configures the password encoder for secure password storage and validation.
     * Uses BCrypt with strength 10 for strong password hashing with automatic salt generation.
     *
     * @return PasswordEncoder using BCrypt algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creating BCryptPasswordEncoder bean");
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures custom handler for access denied (403 Forbidden) responses.
     * Returns JSON response instead of default HTML error page for API consistency.
     *
     * @return AccessDeniedHandler that returns JSON error responses
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        log.debug("Creating custom AccessDeniedHandler for 403 responses");

        return (request, response, accessDeniedException) -> {
            String requestUri = request.getRequestURI();
            String method = request.getMethod();

            log.warn("Access denied for {} {}: {}", method, requestUri, accessDeniedException.getMessage());

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
        };
    }

    /**
     * Configures custom handler for authentication failure (401 Unauthorized) responses.
     * Returns JSON response for unauthenticated requests to protected endpoints.
     *
     * @return AuthenticationEntryPoint that returns JSON error responses
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        log.debug("Creating custom AuthenticationEntryPoint for 401 responses");

        return (request, response, authException) -> {
            String requestUri = request.getRequestURI();
            String method = request.getMethod();

            log.warn("Authentication required for {} {}: {}", method, requestUri, authException.getMessage());

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
        };
    }

}