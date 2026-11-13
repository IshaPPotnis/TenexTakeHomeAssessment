package com.tenextakehome.dto;

import jakarta.validation.constraints.NotBlank;

public class DetectionRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotBlank(message = "Rule logic is required")
    private String ruleLogic;

    @NotBlank(message = "Rule description is required")
    private String description;

    public DetectionRuleRequest() {
    }

    public DetectionRuleRequest(String name, String ruleLogic, String description) {
        this.name = name;
        this.ruleLogic = ruleLogic;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuleLogic() {
        return ruleLogic;
    }

    public void setRuleLogic(String ruleLogic) {
        this.ruleLogic = ruleLogic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
