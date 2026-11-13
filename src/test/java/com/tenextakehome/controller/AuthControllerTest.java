package com.tenextakehome.controller;

import com.tenextakehome.dto.LoginRequest;
import com.tenextakehome.service.AuthTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final AuthController controller = new AuthController(authTokenService);

    @Test
    void loginReturnsBearerTokenForConfiguredCustomer() {
        when(authTokenService.issueToken("admin-dev")).thenReturn("issued-token");
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin-dev");
        loginRequest.setPassword("admin@123");

        var response = controller.login(loginRequest);

        assertEquals("admin-dev", response.getBody().getCustomerName());
        assertEquals("issued-token", response.getBody().getAccessToken());
        org.mockito.Mockito.verify(authTokenService).issueToken("admin-dev");
    }

    @Test
    void loginRejectsIncorrectCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin-dev");
        loginRequest.setPassword("incorrect");

        assertThrows(ResponseStatusException.class, () -> controller.login(loginRequest));
    }
}
