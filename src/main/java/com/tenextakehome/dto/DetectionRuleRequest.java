package com.tenextakehome.dto;

import com.tenextakehome.validation.ValidRuleLogic;
import com.tenextakehome.model.DetectionRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public class DetectionRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotBlank(message = "Rule logic is required")
    @ValidRuleLogic
    private String ruleLogic;

    @NotNull(message = "Rule type is required")
    private DetectionRuleType type = DetectionRuleType.SINGLE_EVENT;

    @ValidRuleLogic
    private String followedByRuleLogic;

    @Min(value = 1, message = "Required occurrences must be at least one")
    private Integer requiredOccurrences = 1;
    @Min(value = 1, message = "Followed-by occurrences must be at least one")
    private Integer followedByRequiredOccurrences = 1;

    @NotBlank(message = "Rule description is required")
    private String description;

    public DetectionRuleRequest() {
    }

    public DetectionRuleRequest(String name, String ruleLogic, String description) {
        this(name, ruleLogic, null, DetectionRuleType.SINGLE_EVENT, 1, description);
    }

    public DetectionRuleRequest(
            String name,
            String ruleLogic,
            String followedByRuleLogic,
            DetectionRuleType type,
            String description) {
        this(name, ruleLogic, followedByRuleLogic, type, 1, description);
    }

    public DetectionRuleRequest(
            String name,
            String ruleLogic,
            String followedByRuleLogic,
            DetectionRuleType type,
            Integer requiredOccurrences,
            String description) {
        this(name, ruleLogic, followedByRuleLogic, type, requiredOccurrences, 1, description);
    }

    public DetectionRuleRequest(
            String name,
            String ruleLogic,
            String followedByRuleLogic,
            DetectionRuleType type,
            Integer requiredOccurrences,
            Integer followedByRequiredOccurrences,
            String description) {
        this.name = name;
        this.ruleLogic = ruleLogic;
        this.followedByRuleLogic = followedByRuleLogic;
        setType(type);
        setRequiredOccurrences(requiredOccurrences);
        setFollowedByRequiredOccurrences(followedByRequiredOccurrences);
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

    public DetectionRuleType getType() {
        return type;
    }

    public void setType(DetectionRuleType type) {
        this.type = type;
    }

    public String getFollowedByRuleLogic() {
        return followedByRuleLogic;
    }

    public void setFollowedByRuleLogic(String followedByRuleLogic) {
        this.followedByRuleLogic = followedByRuleLogic;
    }

    public Integer getRequiredOccurrences() {
        return requiredOccurrences == null ? 1 : requiredOccurrences;
    }

    public void setRequiredOccurrences(Integer requiredOccurrences) {
        this.requiredOccurrences = requiredOccurrences == null ? 1 : requiredOccurrences;
    }

    public Integer getFollowedByRequiredOccurrences() {
        return followedByRequiredOccurrences == null ? 1 : followedByRequiredOccurrences;
    }

    public void setFollowedByRequiredOccurrences(Integer followedByRequiredOccurrences) {
        this.followedByRequiredOccurrences =
                followedByRequiredOccurrences == null ? 1 : followedByRequiredOccurrences;
    }

    @AssertTrue(message = "followedByRuleLogic is required for FOLLOWED_BY rules")
    public boolean isFollowedByRuleLogicValid() {
        return type != DetectionRuleType.FOLLOWED_BY
                || (followedByRuleLogic != null && !followedByRuleLogic.isBlank());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
