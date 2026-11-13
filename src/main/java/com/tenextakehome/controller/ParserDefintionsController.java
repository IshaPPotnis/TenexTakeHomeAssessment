package com.tenextakehome.controller;

import com.tenextakehome.dto.ParserDefinitionRequest;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.service.AuthTokenService;
import com.tenextakehome.service.DefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/definitions")
@Tag(name = "Definitions", description = "CRUD operations for parser definitions")
public class ParserDefintionsController {

    private final DefinitionService definitionService;

    public ParserDefintionsController(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping
    @Operation(summary = "Get all parser definitions")
    public ResponseEntity<List<ParserDefinition>> getAllDefinitions(
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        return ResponseEntity.ok(definitionService.getAllDefinitions(customerName));
    }

    @GetMapping("/{parserId}")
    @Operation(summary = "Get a parser definition by ID")
    public ResponseEntity<ParserDefinition> getDefinition(
            @PathVariable Long parserId,
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        return ResponseEntity.ok(definitionService.getDefinition(parserId, customerName));
    }

    @PostMapping
    @Operation(summary = "Create a parser definition")
    public ResponseEntity<ParserDefinition> createDefinition(
            @Valid @RequestBody ParserDefinitionRequest request,
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        ParserDefinition createdDefinition = definitionService.createDefinition(request, customerName);
        return ResponseEntity.created(URI.create("/api/definitions/" + createdDefinition.getParserId()))
                .body(createdDefinition);
    }

    @PutMapping("/{parserId}")
    @Operation(summary = "Update a parser definition")
    public ResponseEntity<ParserDefinition> updateDefinition(
            @PathVariable Long parserId,
            @Valid @RequestBody ParserDefinitionRequest request,
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        return ResponseEntity.ok(definitionService.updateDefinition(parserId, request, customerName));
    }

    @DeleteMapping("/{parserId}")
    @Operation(summary = "Delete a parser definition")
    public ResponseEntity<Void> deleteDefinition(
            @PathVariable Long parserId,
            @RequestHeader(AuthTokenService.CUSTOMER_NAME_HEADER) String customerName) {
        definitionService.deleteDefinition(parserId, customerName);
        return ResponseEntity.noContent().build();
    }
}
