package com.tenextakehome.config;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.service.DetectionRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DetectionRuleInitializerTest {

    private final DetectionRuleService detectionRuleService = mock(DetectionRuleService.class);
    private final DetectionRuleInitializer initializer = new DetectionRuleInitializer(detectionRuleService);

    @Test
    void initializesFailedLoginRuleWithExpectedDefinition() {
        initializer.run(null);

        ArgumentCaptor<DetectionRuleRequest> requestCaptor = ArgumentCaptor.forClass(DetectionRuleRequest.class);
        verify(detectionRuleService).initializeGlobalRule(requestCaptor.capture());
        DetectionRuleRequest request = requestCaptor.getValue();
        assertEquals("Failed Login attempt", request.getName());
        assertEquals("EventID=\"4625\" AND Channel=\"Security\"", request.getRuleLogic());
        assertEquals(
                "Detects failed login attempts in the Security channel (Windows Event ID 4625).",
                request.getDescription());
    }
}
