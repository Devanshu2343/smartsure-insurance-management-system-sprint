package com.insurance.authservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating and validating JWT tokens.
 * Stores email and role claims used by API Gateway and downstream services.
 */
@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userDetails authenticated user details
     * @return signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        String email = userDetails.getUsername();
        String role = resolvePrimaryRole(userDetails);
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates token signature and expiration.
     *
     * @param token JWT token
     * @return true when token is valid
     */
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Extracts email (username) from JWT.
     *
     * @param token JWT token
     * @return email stored in subject claim
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts role from JWT.
     *
     * @param token JWT token
     * @return role claim value
     */
    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? "" : String.valueOf(role);
    }

    /**
     * Extracts token expiry time.
     *
     * @param token JWT token
     * @return expiry timestamp in local time
     */
    public LocalDateTime extractExpiry(String token) {
        return LocalDateTime.ofInstant(
                extractAllClaims(token).getExpiration().toInstant(),
                ZoneId.systemDefault());
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String resolvePrimaryRole(UserDetails userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse("CUSTOMER");
    }
}
