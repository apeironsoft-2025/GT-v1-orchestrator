package com.apeironsoft.GT_v1_orchestrator.rule_engine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleSummaryGenerateResponse {

    private String id;

    private String ruleId;

    private String ruleName;

    private String inputTradesPath;

    private String outputSummaryPath;

    private Integer exitCode;

    private String message;
}
