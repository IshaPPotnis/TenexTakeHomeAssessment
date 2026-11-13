package com.tenextakehome.config;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.model.CustomerScope;
import com.tenextakehome.service.DetectionRuleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DetectionRuleInitializer implements ApplicationRunner {

    private static final String RULE_NAME = "Failed Login attempt";
    private static final String RULE_LOGIC = "EventID=4625 AND Channel=Security";
    private static final String RULE_DESCRIPTION =
            "Detects failed login attempts in the Security channel (Windows Event ID 4625).";

    private final DetectionRuleService detectionRuleService;

    public DetectionRuleInitializer(DetectionRuleService detectionRuleService) {
        this.detectionRuleService = detectionRuleService;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean ruleExists = detectionRuleService.getAllRules().stream()
                .anyMatch(rule -> CustomerScope.isGlobal(rule.getCustomerName())
                        && RULE_NAME.equals(rule.getName()));
        if (!ruleExists) {
            detectionRuleService.createGlobalRule(
                    new DetectionRuleRequest(RULE_NAME, RULE_LOGIC, RULE_DESCRIPTION));
        }
    }
}
