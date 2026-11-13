package com.tenextakehome.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class AuthTokenServiceTest {

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final AuthTokenService authTokenService = new AuthTokenService(redisTemplate);

    AuthTokenServiceTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void issuesRandomBearerTokenStoredWithExpiry() {
        String firstToken = authTokenService.issueToken("admin-dev");
        String secondToken = authTokenService.issueToken("admin-dev");

        assertNotEquals(firstToken, secondToken);
        verify(valueOperations, times(2)).set(anyString(), eq("admin-dev"), eq(Duration.ofHours(8)));
    }

    @Test
    void authenticatesTokenWhenHeaderMatchesStoredCustomerName() {
        String token = authTokenService.issueToken("admin-dev");
        when(valueOperations.get(anyString())).thenReturn("admin-dev");

        authTokenService.authenticate(token, "admin-dev");
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).get(keyCaptor.capture());
        assertTrue(keyCaptor.getValue().startsWith("auth:tokens:"));
        assertNotEquals(token, keyCaptor.getValue());
    }

    @Test
    void rejectsUnknownBearerToken() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> authTokenService.authenticate("unknown", "admin-dev"));
    }

    @Test
    void rejectsCustomerNameHeaderThatDoesNotMatchToken() {
        when(valueOperations.get(anyString())).thenReturn("admin-dev");

        assertThrows(ResponseStatusException.class,
                () -> authTokenService.authenticate("valid-token", "another-customer"));
    }
}
