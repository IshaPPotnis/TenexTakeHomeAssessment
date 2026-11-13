package com.tenextakehome.service;

import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.model.ParserDefinition;
import com.tenextakehome.model.ParserType;
import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.repository.DetectionRuleRepository;
import com.tenextakehome.repository.ParserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerScopeTest {

    @Test
    void parserListContainsOnlyCustomerAndGlobalDefinitions() {
        ParserRepository repository = mock(ParserRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new ParserDefinition("global", 1L, ParserType.JSON),
                customerParser("tenant-a", 2L),
                customerParser("tenant-b", 3L)));
        DefinitionService service = new DefinitionService(repository, mock(StringRedisTemplate.class));

        assertEquals(List.of("global", "tenant-a"), service.getAllDefinitions("tenant-a").stream()
                .map(ParserDefinition::getParserName)
                .toList());
    }

    @Test
    void detectionListContainsOnlyCustomerAndGlobalRules() {
        DetectionRuleRepository repository = mock(DetectionRuleRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new DetectionRule("global", 1000L, "level=ERROR", "Global rule"),
                customerRule("tenant-a", 1001L),
                customerRule("tenant-b", 1002L)));
        DetectionRuleService service = new DetectionRuleService(repository, mock(StringRedisTemplate.class));

        assertEquals(List.of("global", "tenant-a"), service.getAllRules("tenant-a").stream()
                .map(DetectionRule::getName)
                .toList());
    }

    @Test
    void parserDefinitionCannotBeFetchedFromAnotherCustomerScope() {
        ParserRepository repository = mock(ParserRepository.class);
        ParserDefinition definition = customerParser("tenant-b", 3L);
        when(repository.findById(3L)).thenReturn(java.util.Optional.of(definition));
        DefinitionService service = new DefinitionService(repository, mock(StringRedisTemplate.class));

        assertThrows(ResourceNotFoundException.class, () -> service.getDefinition(3L, "tenant-a"));
    }

    @Test
    void detectionRuleCannotBeFetchedFromAnotherCustomerScope() {
        DetectionRuleRepository repository = mock(DetectionRuleRepository.class);
        DetectionRule rule = customerRule("tenant-b", 1002L);
        when(repository.findById(1002L)).thenReturn(java.util.Optional.of(rule));
        DetectionRuleService service = new DetectionRuleService(repository, mock(StringRedisTemplate.class));

        assertThrows(ResourceNotFoundException.class, () -> service.getRule(1002L, "tenant-a"));
    }

    private ParserDefinition customerParser(String customerName, Long id) {
        ParserDefinition definition = new ParserDefinition(customerName, id, ParserType.JSON);
        definition.setCustomerName(customerName);
        return definition;
    }

    private DetectionRule customerRule(String customerName, Long id) {
        DetectionRule rule = new DetectionRule(customerName, id, "level=ERROR", "Customer rule");
        rule.setCustomerName(customerName);
        return rule;
    }
}
