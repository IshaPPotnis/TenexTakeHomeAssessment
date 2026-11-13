package com.tenextakehome.config;

import com.tenextakehome.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BearerTokenAuthInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;

    public BearerTokenAuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        String token = AuthTokenService.tokenFromAuthorizationHeader(request.getHeader("Authorization"));
        try {
            authTokenService.authenticate(token, request.getHeader(AuthTokenService.CUSTOMER_NAME_HEADER));
            return true;
        } catch (org.springframework.web.server.ResponseStatusException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }
}
