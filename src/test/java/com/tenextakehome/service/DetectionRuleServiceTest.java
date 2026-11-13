package com.tenextakehome.service;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.repository.DetectionRuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetectionRuleServiceTest {

    private final DetectionRuleRepository repository = mock(DetectionRuleRepository.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final DetectionRuleService service = new DetectionRuleService(repository, redisTemplate);

    @Test
    void initializesExistingGlobalRuleToDefaultValuesAndKeepsItsId() {
        DetectionRule existing = new DetectionRule(
                "Failed Login attempt", 1000L, "old condition", "old description");
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DetectionRule result = service.initializeGlobalRule(new DetectionRuleRequest(
                "Failed Login attempt",
                "EventID=\"4625\" AND Channel=\"Security\"",
                "Default description"));

        assertEquals(1000L, result.getId());
        assertEquals("EventID=\"4625\" AND Channel=\"Security\"", result.getRuleLogic());
        assertEquals("Default description", result.getDescription());
        verify(repository).save(existing);
    }
}
