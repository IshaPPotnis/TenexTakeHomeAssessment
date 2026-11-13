package com.tenextakehome.controller;

import com.tenextakehome.dto.AuthSessionResponse;
import com.tenextakehome.dto.LoginRequest;
import com.tenextakehome.service.AuthTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String USERNAME = "admin-dev";
    private static final String PASSWORD = "admin@123";

    private final AuthTokenService authTokenService;

    public AuthController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthSessionResponse> login(
            @Valid @RequestBody LoginRequest request) {
        if (!USERNAME.equals(request.getUsername()) || !PASSWORD.equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        String token = authTokenService.issueToken(USERNAME);
        return ResponseEntity.ok(new AuthSessionResponse(USERNAME, token));
    }

    @GetMapping("/session")
    public ResponseEntity<AuthSessionResponse> getSession(
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        return ResponseEntity.ok(new AuthSessionResponse(customerName));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = AuthTokenService.tokenFromAuthorizationHeader(authorizationHeader);
        authTokenService.revokeToken(token);
        return ResponseEntity.noContent().build();
    }
}
