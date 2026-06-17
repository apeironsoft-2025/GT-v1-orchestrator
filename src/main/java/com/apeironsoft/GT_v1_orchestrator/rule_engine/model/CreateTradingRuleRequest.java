package com.apeironsoft.GT_v1_orchestrator.rule_engine.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateTradingRuleRequest {

    @NotBlank
    private String ruleId;

    @NotBlank
    private String ruleName;

    @NotNull
    @NotEmpty
    private Map<String, Object> fullRuleConditions;
}
