package com.tenextakehome.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RuleLogicValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRuleLogic {

    String message() default "Rule logic is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
