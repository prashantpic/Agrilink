package com.thesss.platform.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKeyInstance;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        if (jwtProperties.getSecretKey() == null || jwtProperties.getSecretKey().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key cannot be null or empty. Please configure common.jwt.secret-key.");
        }
        // Ensure the key is securely managed and has sufficient length (e.g., 256 bits for HS256)
        // JJWT's Keys.hmacShaKeyFor will generate a key of appropriate size for the algorithm if the input is too short,
        // but it's better to provide a strong key.
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        this.secretKeyInstance = Keys.hmacShaKeyFor(keyBytes); // For HS256, key should be >= 256 bits (32 bytes)
        log.info("JwtTokenProvider initialized. Access token expiration: {} ms, Refresh token expiration: {} ms, Issuer: {}",
                 jwtProperties.getAccessTokenExpirationMs(), jwtProperties.getRefreshTokenExpirationMs(), jwtProperties.getIssuer());
    }

    public String generateAccessToken(AuthenticatedPrincipal principal) {
        return generateToken(principal, jwtProperties.getAccessTokenExpirationMs(), "access_token");
    }

    public String generateRefreshToken(AuthenticatedPrincipal principal) {
        return generateToken(principal, jwtProperties.getRefreshTokenExpirationMs(), "refresh_token");
    }

    private String generateToken(AuthenticatedPrincipal principal, long expirationMs, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", principal.getUserId());
        claims.put("roles", principal.getRoles());
        claims.put("permissions", principal.getPermissions());
        principal.getTenantId().ifPresent(tenantId -> claims.put("tenantId", tenantId));
        claims.put("type", tokenType); // Differentiate token types if needed

        Instant now = Instant.now();
        Instant expirationInstant = now.plusMillis(expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationInstant))
                .setIssuer(jwtProperties.getIssuer())
                .signWith(secretKeyInstance, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKeyInstance)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage()); // Warn, as this is an expected condition
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty or invalid: {}", ex.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unchecked") // For casting claims to List<String>
    public AuthenticatedPrincipal getPrincipalFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }

        try {
            String userId = claims.get("userId", String.class);
            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            List<String> permissions = claims.get("permissions", List.class);
            String tenantId = claims.get("tenantId", String.class);

            if (userId == null || username == null || roles == null || permissions == null) {
                log.warn("JWT is missing required claims (userId, username, roles, permissions).");
                return null;
            }
            return new AuthenticatedPrincipal(userId, username, roles, permissions, tenantId);
        } catch (Exception e) {
            log.error("Error extracting principal from JWT claims: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
         try {
             final Date expiration = getExpirationDateFromToken(token);
             return expiration != null && expiration.before(new Date());
         } catch (ExpiredJwtException ex) {
             return true; // Token is explicitly expired
         } catch (Exception e) {
             log.error("Error checking token expiration for token (potentially malformed or invalid): {}", e.getMessage());
             return true; // Treat as expired or invalid if any parsing error occurs
         }
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKeyInstance)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // If the token is expired, we can still get claims for audit/logging purposes
            log.warn("Attempting to parse claims from an expired JWT token: {}", e.getMessage());
            return e.getClaims();
        } catch (JwtException e) { // Catches other JWT-related exceptions (Malformed, Signature, etc.)
            log.error("Failed to parse JWT claims: {}", e.getMessage());
            return null;
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        if (claims == null) return null;
        try {
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.error("Error resolving claim from token: {}", e.getMessage());
            return null;
        }
    }
}