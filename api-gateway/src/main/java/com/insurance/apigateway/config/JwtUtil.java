package com.insurance.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT utility for API Gateway validation and claim extraction.
 * Gateway is the single security enforcement layer in the system.
 */
@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /**
     * Validates token signature and expiration.
     *
     * @param token JWT string
     * @return true when token is valid
     */
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Extracts email claim or subject as fallback.
     *
     * @param token JWT string
     * @return email identifier
     */
    public String extractEmail(String token) {
        Object email = extractAllClaims(token).get("email");
        return email == null ? extractAllClaims(token).getSubject() : String.valueOf(email);
    }

    /**
     * Extracts role claim from token.
     *
     * @param token JWT string
     * @return role value
     */
    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? "" : String.valueOf(role);
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
}
