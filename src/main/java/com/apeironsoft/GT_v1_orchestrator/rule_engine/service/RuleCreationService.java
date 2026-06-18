package com.apeironsoft.GT_v1_orchestrator.rule_engine.service;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.TradingRule;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.CreateTradingRuleRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.repository.TradingRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleCreationService {

    private static final String DEFAULT_RULE_SET_ID = "DEFAULT_RULE_SET";
    private static final String DEFAULT_RULE_SET_NAME = "Default Rule Set";
    private static final String DEFAULT_EXECUTOR_TYPE = "RULE_01_DEFAULT";

    private final TradingRuleRepository tradingRuleRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.storage.rule-json-dir}")
    private String ruleJsonDir;

    public TradingRule saveRule(CreateTradingRuleRequest request) {

        validateCreateRuleRequest(request);

        if (tradingRuleRepository.existsByRuleId(request.getRuleId().trim())) {
            throw new IllegalArgumentException("Rule already exists with ruleId: " + request.getRuleId());
        }

        TradingRule tradingRule = TradingRule.builder()
                .ruleId(request.getRuleId().trim())
                .ruleName(request.getRuleName().trim())
                .ruleSetId(resolveOptionalRuleValue(request.getRuleSetId(), request.getFullRuleConditions(), "ruleSetId", DEFAULT_RULE_SET_ID))
                .ruleSetName(resolveOptionalRuleValue(request.getRuleSetName(), request.getFullRuleConditions(), "ruleSetName", DEFAULT_RULE_SET_NAME))
                .executorType(resolveOptionalRuleValue(request.getExecutorType(), request.getFullRuleConditions(), "executorType", DEFAULT_EXECUTOR_TYPE))
                .fullRuleConditions(request.getFullRuleConditions())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TradingRule savedRule = tradingRuleRepository.save(tradingRule);
        writeRuleJsonFile(savedRule);
        Path savedRuleJsonPath = buildRuleJsonPath(savedRule.getRuleId());

        log.info("Trading rule saved successfully. ruleId={}, mongoId={}, jsonPath={}",
                savedRule.getRuleId(),
                savedRule.getId(),
                savedRuleJsonPath);

        return savedRule;
    }

    public List<TradingRule> getAllRules() {
        List<TradingRule> rules = tradingRuleRepository.findAll();
        log.info("Fetched all trading rules. count={}", rules.size());
        return rules;
    }

    public TradingRule getRuleByRuleId(String ruleId) {

        if (!StringUtils.hasText(ruleId)) {
            throw new IllegalArgumentException("ruleId is required");
        }

        return tradingRuleRepository.findByRuleId(ruleId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Rule not found for ruleId: " + ruleId));
    }

    private void validateCreateRuleRequest(CreateTradingRuleRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("CreateTradingRuleRequest is required");
        }

        if (!StringUtils.hasText(request.getRuleId())) {
            throw new IllegalArgumentException("ruleId is required");
        }

        if (!StringUtils.hasText(request.getRuleName())) {
            throw new IllegalArgumentException("ruleName is required");
        }

        Map<String, Object> fullRuleConditions = request.getFullRuleConditions();

        if (fullRuleConditions == null || fullRuleConditions.isEmpty()) {
            throw new IllegalArgumentException("fullRuleConditions is required and must be a valid JSON object");
        }

        validateRequiredJsonSection(fullRuleConditions, "input");
        validateRequiredJsonSection(fullRuleConditions, "parameters");
        validateRequiredJsonSection(fullRuleConditions, "entryLogic");
        validateRequiredJsonSection(fullRuleConditions, "executionLogic");
        validateRequiredJsonSection(fullRuleConditions, "tpSlLogic");
        validateRequiredJsonSection(fullRuleConditions, "violationLogic");
    }

    private void validateRequiredJsonSection(Map<String, Object> fullRuleConditions, String sectionName) {

        Object section = fullRuleConditions.get(sectionName);

        if (!(section instanceof Map)) {
            throw new IllegalArgumentException(
                    "fullRuleConditions." + sectionName + " is required and must be a JSON object"
            );
        }
    }

    private String resolveOptionalRuleValue(
            String topLevelValue,
            Map<String, Object> fullRuleConditions,
            String fullRuleConditionsKey,
            String defaultValue
    ) {
        if (StringUtils.hasText(topLevelValue)) {
            return topLevelValue.trim();
        }

        Object fullRuleConditionsValue = fullRuleConditions.get(fullRuleConditionsKey);
        if (fullRuleConditionsValue instanceof String value && StringUtils.hasText(value)) {
            return value.trim();
        }

        return defaultValue;
    }

    private Path buildRuleJsonPath(String ruleId) {
        String fileName = sanitizeFileName(ruleId) + ".json";
        Path rulesDirectory = Path.of(ruleJsonDir).toAbsolutePath().normalize();
        return rulesDirectory.resolve(fileName).normalize();
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private void writeRuleJsonFile(TradingRule savedRule) {
        Path ruleJsonPath = buildRuleJsonPath(savedRule.getRuleId());
        Map<String, Object> ruleJsonPayload = new LinkedHashMap<>();
        ruleJsonPayload.put("ruleId", savedRule.getRuleId());
        ruleJsonPayload.put("ruleName", savedRule.getRuleName());
        ruleJsonPayload.put("ruleSetId", savedRule.getRuleSetId());
        ruleJsonPayload.put("ruleSetName", savedRule.getRuleSetName());
        ruleJsonPayload.put("executorType", savedRule.getExecutorType());
        ruleJsonPayload.put("fullRuleConditions", savedRule.getFullRuleConditions());

        try {
            Files.createDirectories(ruleJsonPath.getParent());
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ruleJsonPayload);
            Files.writeString(ruleJsonPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Trading rule was saved to MongoDB but failed to write rule JSON file: " + ruleJsonPath,
                    e
            );
        }
    }
}
