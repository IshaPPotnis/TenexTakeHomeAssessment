package com.tenextakehome.config;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.service.DetectionRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetectionRuleInitializerTest {

    private final DetectionRuleService detectionRuleService = mock(DetectionRuleService.class);
    private final DetectionRuleInitializer initializer = new DetectionRuleInitializer(detectionRuleService);

    @Test
    void createsFailedLoginRuleWhenItDoesNotExist() {
        when(detectionRuleService.getAllRules()).thenReturn(List.of());

        initializer.run(null);

        ArgumentCaptor<DetectionRuleRequest> requestCaptor = ArgumentCaptor.forClass(DetectionRuleRequest.class);
        verify(detectionRuleService).createGlobalRule(requestCaptor.capture());
        DetectionRuleRequest request = requestCaptor.getValue();
        assertEquals("Failed Login attempt", request.getName());
        assertEquals("EventID=4625 AND Channel=Security", request.getRuleLogic());
        assertEquals(
                "Detects failed login attempts in the Security channel (Windows Event ID 4625).",
                request.getDescription());
    }

    @Test
    void doesNotCreateFailedLoginRuleWhenItAlreadyExists() {
        when(detectionRuleService.getAllRules()).thenReturn(List.of(
                new DetectionRule("Failed Login attempt", 1000L, "existing condition", "existing description")));

        initializer.run(null);

        verify(detectionRuleService, never()).createGlobalRule(org.mockito.ArgumentMatchers.any());
    }
}
