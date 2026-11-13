package com.tenextakehome.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
public class AuthTokenService {

    public static final String CUSTOMER_NAME_HEADER = "X-Customer-Name";
    private static final String TOKEN_KEY_PREFIX = "auth:tokens:";
    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(String customerName) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        redisTemplate.opsForValue().set(tokenKey(token), customerName, TOKEN_TTL);
        return token;
    }

    public void authenticate(String token, String customerName) {
        if (token == null || token.isBlank() || customerName == null || customerName.isBlank()) {
            throw unauthorized();
        }
        String tokenCustomerName = redisTemplate.opsForValue().get(tokenKey(token));
        if (tokenCustomerName == null || !tokenCustomerName.equals(customerName)) {
            throw unauthorized();
        }
    }

    public void revokeToken(String token) {
        if (token != null && !token.isBlank()) {
            redisTemplate.delete(tokenKey(token));
        }
    }

    public static String tokenFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String tokenKey(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return TOKEN_KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A valid bearer token is required");
    }
}
