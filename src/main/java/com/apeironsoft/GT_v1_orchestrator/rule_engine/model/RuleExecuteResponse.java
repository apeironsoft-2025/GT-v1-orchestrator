package com.apeironsoft.GT_v1_orchestrator.rule_engine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleExecuteResponse {
    private String ruleId;
    private String ruleName;
    private String executorType;
    private String datasetLocation;
    private String ruleJsonPath;
    private String outputTradesPath;
    private Integer exitCode;
    private String message;
}
