package com.tenextakehome.validation;

import com.tenextakehome.service.RuleEvaluator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RuleLogicValidator implements ConstraintValidator<ValidRuleLogic, String> {

    @Override
    public boolean isValid(String ruleLogic, ConstraintValidatorContext context) {
        if (ruleLogic == null || ruleLogic.isBlank()) {
            return true;
        }
        try {
            RuleEvaluator.evaluate(ruleLogic);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
