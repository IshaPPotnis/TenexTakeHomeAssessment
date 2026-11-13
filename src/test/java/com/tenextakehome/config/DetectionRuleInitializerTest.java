package com.tenextakehome.config;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.model.DetectionRuleType;
import com.tenextakehome.service.DetectionRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class DetectionRuleInitializerTest {

    private final DetectionRuleService detectionRuleService = mock(DetectionRuleService.class);
    private final DetectionRuleInitializer initializer = new DetectionRuleInitializer(detectionRuleService);

    @Test
    void initializesSingleEventAndFiveFailuresFollowedBySuccessRules() {
        initializer.run(null);

        ArgumentCaptor<DetectionRuleRequest> requestCaptor = ArgumentCaptor.forClass(DetectionRuleRequest.class);
        verify(detectionRuleService, times(2)).initializeGlobalRule(requestCaptor.capture());
        DetectionRuleRequest singleEventRule = requestCaptor.getAllValues().get(0);
        assertEquals("Failed Login attempt", singleEventRule.getName());
        assertEquals("EventID=\"4625\" AND Channel=\"Security\"", singleEventRule.getRuleLogic());
        assertEquals(
                "Detects failed login attempts in the Security channel (Windows Event ID 4625).",
                singleEventRule.getDescription());

        DetectionRuleRequest followedByRule = requestCaptor.getAllValues().get(1);
        assertEquals("Five Failed Logins Followed By Success", followedByRule.getName());
        assertEquals("EventID=\"4625\"", followedByRule.getRuleLogic());
        assertEquals("EventID=\"4624\"", followedByRule.getFollowedByRuleLogic());
        assertEquals(DetectionRuleType.FOLLOWED_BY, followedByRule.getType());
        assertEquals(5, followedByRule.getRequiredOccurrences());
        assertEquals(1, followedByRule.getFollowedByRequiredOccurrences());
    }
}
