package com.tenextakehome.config;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.service.DetectionRuleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DetectionRuleInitializer implements ApplicationRunner {

    private static final String RULE_NAME = "Failed Login attempt";
    private static final String RULE_LOGIC = "EventID=\"4625\" AND Channel=\"Security\"";
    private static final String RULE_DESCRIPTION =
            "Detects failed login attempts in the Security channel (Windows Event ID 4625).";

    private final DetectionRuleService detectionRuleService;

    public DetectionRuleInitializer(DetectionRuleService detectionRuleService) {
        this.detectionRuleService = detectionRuleService;
    }

    @Override
    public void run(ApplicationArguments args) {
        detectionRuleService.initializeGlobalRule(
                new DetectionRuleRequest(RULE_NAME, RULE_LOGIC, RULE_DESCRIPTION));
    }
}
