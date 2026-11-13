package com.tenextakehome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEvaluatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void evaluatesFailedLoginRuleAgainstMatchingLog() throws Exception {
        Predicate<JsonNode> rule = RuleEvaluator.evaluate("EventID=4625 AND Channel=Security");
        JsonNode log = objectMapper.readTree("{\"EventID\":4625,\"Channel\":\"Security\"}");

        assertTrue(rule.test(log));
    }

    @Test
    void returnsFalseWhenOneAndConditionDoesNotMatch() throws Exception {
        Predicate<JsonNode> rule = RuleEvaluator.evaluate("EventID=4625 AND Channel=Security");
        JsonNode log = objectMapper.readTree("{\"EventID\":4625,\"Channel\":\"System\"}");

        assertFalse(rule.test(log));
    }

    @Test
    void evaluatesNestedFieldsAndOrConditions() throws Exception {
        Predicate<JsonNode> rule = RuleEvaluator.evaluate(
                "event.id EQUAL 4625 OR event.channel EQUAL Security");
        JsonNode log = objectMapper.readTree("{\"event\":{\"id\":4625,\"channel\":\"System\"}}");

        assertTrue(rule.test(log));
    }

    @Test
    void orderedComparisonWithMissingOrIncompatibleFieldDoesNotMatch() throws Exception {
        Predicate<JsonNode> missingFieldRule = RuleEvaluator.evaluate("level > 4");
        Predicate<JsonNode> incompatibleFieldRule = RuleEvaluator.evaluate("level > 4");

        assertFalse(missingFieldRule.test(objectMapper.readTree("{\"message\":\"error\"}")));
        assertFalse(incompatibleFieldRule.test(objectMapper.readTree("{\"level\":\"high\"}")));
    }

    @Test
    void rejectsMalformedRuleLogic() {
        assertThrows(IllegalArgumentException.class,
                () -> RuleEvaluator.evaluate("EventID=4625 AND"));
        assertThrows(IllegalArgumentException.class,
                () -> RuleEvaluator.evaluate("EventID LIKE 4625"));
    }
}
