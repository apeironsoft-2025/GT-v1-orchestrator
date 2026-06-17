package com.apeironsoft.GT_v1_orchestrator.rule_engine.model;

import lombok.Data;

@Data
public class RuleExecuteRequest {
    private String ruleId;
    private String datasetLocation;
    private String pair;
}
