package com.apeironsoft.GT_v1_orchestrator.rule_engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "rule_execution_summaries")
public class RuleExecutionSummary {

    @Id
    private String id;

    @Indexed
    private String ruleId;

    private String ruleName;

    private String inputTradesPath;

    private String summaryJsonPath;

    private Map<String, Object> summaryData;

    @Indexed
    private Instant createdAt;

    private Instant updatedAt;
}
