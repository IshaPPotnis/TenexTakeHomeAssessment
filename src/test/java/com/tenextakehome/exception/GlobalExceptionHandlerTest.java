package com.tenextakehome.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void returnsStructuredInternalServerErrorForLogProcessingFailure() {
        var response = handler.handleLogProcessing(
                new LogProcessingException("Unable to parse XML log", new IllegalArgumentException()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("Unable to parse XML log", response.getBody().get("message"));
    }
}
