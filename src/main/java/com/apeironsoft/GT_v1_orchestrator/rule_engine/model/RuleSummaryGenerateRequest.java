package com.apeironsoft.GT_v1_orchestrator.rule_engine.model;

import lombok.Data;

@Data
public class RuleSummaryGenerateRequest {

    private String ruleId;

    private String inputTradesPath;

    private String ruleName;
}
