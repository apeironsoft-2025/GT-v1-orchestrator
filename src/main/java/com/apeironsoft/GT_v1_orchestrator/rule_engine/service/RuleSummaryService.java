package com.apeironsoft.GT_v1_orchestrator.rule_engine.service;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.RuleExecutionSummary;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.TradingRule;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateResponse;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.repository.RuleExecutionSummaryRepository;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.repository.TradingRuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleSummaryService {

    private final RuleExecutionSummaryRepository ruleExecutionSummaryRepository;
    private final TradingRuleRepository tradingRuleRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.python.rule-summary-script}")
    private String summaryScriptPath;

    @Value("${app.storage.rule-output-dir}")
    private String ruleOutputDir;

    public RuleSummaryGenerateResponse generateSummaryAndSave(RuleSummaryGenerateRequest request) {
        validateGenerateRequest(request);

        String ruleId = request.getRuleId().trim();
        Path inputTradesPath = buildInputTradesPath(ruleId);
        return generateSummaryAndSave(request, inputTradesPath);
    }

    RuleSummaryGenerateResponse generateSummaryAndSave(RuleSummaryGenerateRequest request, Path inputTradesPath) {
        validateGenerateRequest(request);

        String ruleId = request.getRuleId().trim();
        Path resolvedInputTradesPath = inputTradesPath.toAbsolutePath().normalize();
        Path summaryScript = Path.of(summaryScriptPath).toAbsolutePath().normalize();

        validateInputTradesPathForRule(ruleId, resolvedInputTradesPath);
        validateExistingFile(resolvedInputTradesPath, "Input trades file does not exist");

        TradingRule tradingRule = tradingRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found for ruleId: " + ruleId));

        String resolvedRuleName = resolveRuleName(tradingRule, ruleId);
        validateTradeCsvRuleNames(ruleId, resolvedRuleName, resolvedInputTradesPath);
        validateExistingFile(summaryScript, "Summary Python script does not exist");

        Path outputSummaryPath = buildOutputSummaryPath(ruleId);

        try {
            Files.createDirectories(outputSummaryPath.getParent());
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("Rule output path parent is not a directory: " + outputSummaryPath.getParent(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create rule output directory: " + outputSummaryPath.getParent(), e);
        }

        int exitCode = runSummaryCommand(buildSummaryCommand(
                summaryScript,
                resolvedInputTradesPath,
                outputSummaryPath,
                ruleId,
                resolvedRuleName
        ));

        validateExistingFile(outputSummaryPath, "Generated summary JSON file does not exist");

        Map<String, Object> summaryData = readSummaryData(outputSummaryPath);
        validateSummaryData(ruleId, resolvedRuleName, resolvedInputTradesPath, summaryData);
        Instant now = Instant.now();

        RuleExecutionSummary summary = RuleExecutionSummary.builder()
                .ruleId(ruleId)
                .ruleName(resolvedRuleName)
                .inputTradesPath(resolvedInputTradesPath.toString())
                .summaryJsonPath(outputSummaryPath.toString())
                .summaryData(summaryData)
                .createdAt(now)
                .updatedAt(now)
                .build();

        RuleExecutionSummary savedSummary = ruleExecutionSummaryRepository.save(summary);
        log.info("Rule execution summary saved. ruleId={}, summaryId={}, summaryJsonPath={}",
                savedSummary.getRuleId(),
                savedSummary.getId(),
                savedSummary.getSummaryJsonPath());

        return RuleSummaryGenerateResponse.builder()
                .id(savedSummary.getId())
                .ruleId(savedSummary.getRuleId())
                .ruleName(savedSummary.getRuleName())
                .inputTradesPath(savedSummary.getInputTradesPath())
                .outputSummaryPath(savedSummary.getSummaryJsonPath())
                .exitCode(exitCode)
                .message("Summary JSON generated and saved successfully")
                .build();
    }

    public RuleExecutionSummary getLatestSummaryByRuleId(String ruleId) {
        if (!StringUtils.hasText(ruleId)) {
            throw new IllegalArgumentException("ruleId is required");
        }

        String normalizedRuleId = ruleId.trim();
        return ruleExecutionSummaryRepository.findFirstByRuleIdOrderByCreatedAtDesc(normalizedRuleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule summary not found for ruleId: " + normalizedRuleId));
    }

    public List<RuleExecutionSummary> getAllSummaries() {
        return ruleExecutionSummaryRepository.findAll();
    }

    private void validateGenerateRequest(RuleSummaryGenerateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RuleSummaryGenerateRequest is required");
        }

        if (!StringUtils.hasText(request.getRuleId())) {
            throw new IllegalArgumentException("ruleId is required");
        }
    }

    private void validateExistingFile(Path path, String message) {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(message + ": " + path);
        }
    }

    private Map<String, Object> readSummaryData(Path summaryJsonPath) {
        try {
            return objectMapper.readValue(summaryJsonPath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read summary JSON file: " + summaryJsonPath, e);
        }
    }

    private String resolveRuleName(TradingRule tradingRule, String ruleId) {
        if (StringUtils.hasText(tradingRule.getRuleName())) {
            return tradingRule.getRuleName().trim();
        }

        return ruleId;
    }

    private Path buildOutputSummaryPath(String ruleId) {
        return Path.of(ruleOutputDir)
                .toAbsolutePath()
                .normalize()
                .resolve(sanitizeFileName(ruleId) + "_summary.json")
                .normalize();
    }

    private Path buildInputTradesPath(String ruleId) {
        return Path.of(ruleOutputDir)
                .toAbsolutePath()
                .normalize()
                .resolve(sanitizeFileName(ruleId) + "_trades.csv")
                .normalize();
    }

    private void validateInputTradesPathForRule(String ruleId, Path inputTradesPath) {
        String fileName = inputTradesPath.getFileName() == null ? "" : inputTradesPath.getFileName().toString();
        String expectedRuleIdInFileName = sanitizeFileName(ruleId);
        if (!fileName.contains(expectedRuleIdInFileName)) {
            throw new IllegalArgumentException(
                    "Input trades path filename must contain requested ruleId. ruleId="
                            + ruleId + ", inputTradesPath=" + inputTradesPath
            );
        }
    }

    private void validateTradeCsvRuleNames(String ruleId, String resolvedRuleName, Path inputTradesPath) {
        Set<String> allowedRuleNames = new HashSet<>();
        if (StringUtils.hasText(ruleId)) {
            allowedRuleNames.add(ruleId.trim());
        }
        if (StringUtils.hasText(resolvedRuleName)) {
            allowedRuleNames.add(resolvedRuleName.trim());
        }

        try (BufferedReader reader = Files.newBufferedReader(inputTradesPath, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (!StringUtils.hasText(headerLine)) {
                return;
            }

            List<String> headers = parseCsvLine(headerLine);
            int ruleNameIndex = headers.indexOf("rule_name");
            if (ruleNameIndex < 0) {
                return;
            }

            Set<String> mismatchedRuleNames = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }

                List<String> values = parseCsvLine(line);
                if (ruleNameIndex >= values.size()) {
                    continue;
                }

                String csvRuleName = values.get(ruleNameIndex).trim();
                if (StringUtils.hasText(csvRuleName) && !allowedRuleNames.contains(csvRuleName)) {
                    mismatchedRuleNames.add(csvRuleName);
                }
            }

            if (!mismatchedRuleNames.isEmpty()) {
                throw new IllegalArgumentException(
                        "Input trades CSV rule_name values do not match requested rule. ruleId="
                                + ruleId + ", inputTradesPath=" + inputTradesPath
                                + ", allowedRuleNames=" + allowedRuleNames
                                + ", mismatchedRuleNames=" + mismatchedRuleNames
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate input trades CSV: " + inputTradesPath, e);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (current == ',' && !inQuotes) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        values.add(value.toString());
        return values;
    }

    private void validateSummaryData(
            String ruleId,
            String resolvedRuleName,
            Path inputTradesPath,
            Map<String, Object> summaryData
    ) {
        requireSummaryValue(summaryData, "ruleId", ruleId);
        requireSummaryValue(summaryData, "ruleName", resolvedRuleName);
        requireSummaryValue(summaryData, "inputTradesPath", inputTradesPath.toString());
    }

    private void requireSummaryValue(Map<String, Object> summaryData, String key, String expectedValue) {
        Object actualValue = summaryData.get(key);
        if (!Objects.equals(String.valueOf(actualValue), expectedValue)) {
            throw new IllegalStateException(
                    "Generated summary " + key + " does not match requested rule. expected="
                            + expectedValue + ", actual=" + actualValue
            );
        }
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private List<String> buildSummaryCommand(
            Path summaryScript,
            Path inputTradesPath,
            Path outputSummaryPath,
            String ruleId,
            String resolvedRuleName
    ) {
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add(summaryScript.toString());
        command.add("--input-trades");
        command.add(inputTradesPath.toString());
        command.add("--output-summary");
        command.add(outputSummaryPath.toString());
        command.add("--rule-id");
        command.add(ruleId);
        command.add("--rule-name");
        command.add(resolvedRuleName);
        return command;
    }

    private int runSummaryCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Summary Python script failed with exitCode=" + exitCode + ". Python output: " + output
                );
            }

            log.info("Summary Python script completed successfully. exitCode={}, output={}", exitCode, output);
            return exitCode;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start summary Python script: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Summary Python script was interrupted", e);
        }
    }
}
