package com.tenextakehome.validation;

import com.tenextakehome.dto.DetectionRuleRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleLogicValidatorTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validatorFactory = factory;
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRuleLogic() {
        DetectionRuleRequest request =
                new DetectionRuleRequest("Failed login", "EventID=\"4625\" AND Channel=\"Security\"", "description");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsMalformedRuleLogic() {
        DetectionRuleRequest request =
                new DetectionRuleRequest("Invalid rule", "EventID LIKE 4625", "description");

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> "ruleLogic".equals(violation.getPropertyPath().toString())));
    }
}
