package com.apeironsoft.GT_v1_orchestrator.rule_engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "trading_rules")
public class TradingRule {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ruleId;

    @Indexed
    private String ruleName;

    @Indexed
    private String ruleSetId;

    private String ruleSetName;

    @Indexed
    private String executorType;

    /**
     * Store full dynamic rule JSON here.
     * Do not use JsonNode/ObjectNode for Mongo entity.
     */
    private Map<String, Object> fullRuleConditions;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
