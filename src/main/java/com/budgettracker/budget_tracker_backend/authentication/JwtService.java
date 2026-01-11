package com.budgettracker.budget_tracker_backend.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Service responsible for JWT (JSON Web Token) operations.
 * Handles token generation, validation, and claim extraction.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.security.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt-expiration}")
    private long jwtExpiration;

    /**
     * Generates a new JWT token for the given username and role.
     *
     * @param username The subject (owner) of the token
     * @param role The role to include in token claims
     * @return A signed JWT token as a String
     */
    public String generateToken(String username, String role) {
        log.debug("Generating JWT token for user: {} with role: {}", username, role);

        var claims = new HashMap<String, Object>();
        claims.put("role", role);

        String token = Jwts
                .builder()
                .setIssuer("self") // todo
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("JWT token generated successfully for user: {}, token length: {} chars",
                username, token.length());
        return token;
    }

    /**
     * Validates if a token is still valid for the given user.
     *
     * @param token The JWT token to validate
     * @param userDetails Spring Security UserDetails object
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Validating JWT token for user: {}", userDetails.getUsername());

        try {
            final String subject = extractSubject(token);
            boolean isValid = (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validation successful for user: {}", userDetails.getUsername());
            } else {
                log.warn("Token validation failed for user: {}", userDetails.getUsername());
            }

            return isValid;
        } catch (Exception e) {
            log.warn("Token validation error for user: {} - {}",
                    userDetails.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Extracts a specific string claim from the token.
     *
     * @param token The JWT token
     * @param claim The claim name to extract (e.g., "role")
     * @return The claim value as String
     */
    public String getStringClaim(String token, String claim) {
        log.debug("Extracting claim '{}' from token", claim);
        try {
            return extractAllClaims(token).get(claim, String.class);
        } catch (Exception e) {
            log.warn("Failed to extract claim '{}' from token: {}", claim, e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the subject (username) from the token.
     *
     * @param token The JWT token
     * @return The username/subject
     */
    public String extractSubject(String token) {
        log.debug("Extracting subject from token");
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.warn("Failed to extract subject from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Generic method to extract any claim from the token.
     *
     * @param <T> The type of claim to return
     * @param token The JWT token
     * @param claimsResolver Function to extract specific claim from Claims object
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from token");
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.warn("Failed to extract claim from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if the token has expired.
     *
     * @param token The JWT token
     * @return true if expired, false if still valid
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token The JWT token
     * @return The expiration Date
     */
    private Date extractExpiration(String token) {
        log.debug("Extracting expiration from token");
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            log.warn("Failed to extract expiration from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts all claims from the token body.
     *
     * @param token The JWT token
     * @return Claims object containing all token data
     */
    private Claims extractAllClaims(String token) {
        log.debug("Extracting all claims from token");
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            throw e;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            throw e;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a HS256 Key. Key is an interface.
     * Starting from secretKey we get a byte array
     * of the secret. Then we get the {@link javax.crypto.SecretKey,
     * class that implements the {@link Key } interface.
     *
     * @return  a SecretKey which implements Key.
     */
    private Key getSignInKey() {
        log.debug("Creating signing key from secret");
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to create signing key: {}", e.getMessage());
            throw e;
        }
    }
}