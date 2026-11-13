package com.tenextakehome.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("detection-rules")
public class DetectionRule {

    @NotBlank(message = "Rule name is required")
    private String name;

    @Id
    @Min(value = 1000, message = "Rule ID must be a four-digit number")
    @Max(value = 9999, message = "Rule ID must be a four-digit number")
    private Long id;

    @NotBlank(message = "Rule logic is required")
    private String ruleLogic;

    @NotBlank(message = "Rule description is required")
    private String description;

    private String customerName = CustomerScope.GLOBAL;

    public DetectionRule() {
    }

    public DetectionRule(String name, String ruleLogic, String description) {
        this.name = name;
        this.ruleLogic = ruleLogic;
        this.description = description;
    }

    public DetectionRule(String name, Long id, String ruleLogic, String description) {
        this.name = name;
        this.id = id;
        this.ruleLogic = ruleLogic;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCustomerName() {
        return CustomerScope.isGlobal(customerName) ? CustomerScope.GLOBAL : customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = CustomerScope.isGlobal(customerName) ? CustomerScope.GLOBAL : customerName;
    }
}
