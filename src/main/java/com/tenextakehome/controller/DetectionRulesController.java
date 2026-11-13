package com.tenextakehome.controller;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.service.DetectionRuleService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/detection-rules")
@Tag(name = "Detection Rules", description = "CRUD operations for incident detection rules")
public class DetectionRulesController {

    private final DetectionRuleService detectionRuleService;

    public DetectionRulesController(DetectionRuleService detectionRuleService) {
        this.detectionRuleService = detectionRuleService;
    }

    @GetMapping
    @Operation(summary = "Get all detection rules")
    public ResponseEntity<List<DetectionRule>> getAllRules() {
        return ResponseEntity.ok(detectionRuleService.getAllRules());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a detection rule by ID")
    public ResponseEntity<DetectionRule> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(detectionRuleService.getRule(id));
    }

    @PostMapping
    @Operation(summary = "Create a detection rule")
    public ResponseEntity<DetectionRule> createRule(@Valid @RequestBody DetectionRuleRequest request) {
        DetectionRule createdRule = detectionRuleService.createRule(request);
        return ResponseEntity.created(URI.create("/api/detection-rules/" + createdRule.getId()))
                .body(createdRule);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a detection rule")
    public ResponseEntity<DetectionRule> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody DetectionRuleRequest request) {
        return ResponseEntity.ok(detectionRuleService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a detection rule")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        detectionRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
