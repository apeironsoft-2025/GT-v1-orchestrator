package com.apeironsoft.GT_v1_orchestrator.rule_engine.service;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleExecuteRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleExecuteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleExecutionService {

    @Value("${app.storage.rule-json-dir}")
    private String ruleJsonDir;

    @Value("${app.storage.rule-output-dir}")
    private String ruleOutputDir;

    @Value("${app.python.rule-executor-script}")
    private String ruleExecutorScript;

    public RuleExecuteResponse executeRule(RuleExecuteRequest request) {
        validateRequest(request);

        String ruleId = request.getRuleId().trim();
        Path datasetPath = Path.of(request.getDatasetLocation().trim()).toAbsolutePath().normalize();
        Path ruleJsonPath = buildRuleJsonPath(ruleId);
        Path outputTradesPath = buildOutputTradesPath(ruleId);

        validateExistingFile(datasetPath, "Dataset file not found");
        validateExistingFile(ruleJsonPath, "Rule JSON file not found");

        try {
            Files.createDirectories(outputTradesPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create rule output directory: " + outputTradesPath.getParent(), e);
        }

        List<String> command = buildPythonCommand(request, ruleId, datasetPath, ruleJsonPath, outputTradesPath);
        log.info("Executing rule. ruleId={}, ruleJsonPath={}, datasetPath={}, outputTradesPath={}",
                ruleId,
                ruleJsonPath,
                datasetPath,
                outputTradesPath);

        int exitCode = runPythonCommand(command);

        return RuleExecuteResponse.builder()
                .ruleId(ruleId)
                .datasetLocation(datasetPath.toString())
                .ruleJsonPath(ruleJsonPath.toString())
                .outputTradesPath(outputTradesPath.toString())
                .exitCode(exitCode)
                .message("Trade CSV generated successfully")
                .build();
    }

    private void validateRequest(RuleExecuteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RuleExecuteRequest is required");
        }

        if (!StringUtils.hasText(request.getRuleId())) {
            throw new IllegalArgumentException("ruleId is required");
        }

        if (!StringUtils.hasText(request.getDatasetLocation())) {
            throw new IllegalArgumentException("datasetLocation is required");
        }
    }

    private void validateExistingFile(Path path, String message) {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(message + ": " + path);
        }
    }

    private Path buildRuleJsonPath(String ruleId) {
        return Path.of(ruleJsonDir)
                .toAbsolutePath()
                .normalize()
                .resolve(sanitizeFileName(ruleId) + ".json")
                .normalize();
    }

    private Path buildOutputTradesPath(String ruleId) {
        return Path.of(ruleOutputDir)
                .toAbsolutePath()
                .normalize()
                .resolve(sanitizeFileName(ruleId) + "_trades.csv")
                .normalize();
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private List<String> buildPythonCommand(
            RuleExecuteRequest request,
            String ruleId,
            Path datasetPath,
            Path ruleJsonPath,
            Path outputTradesPath
    ) {
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add(Path.of(ruleExecutorScript).toAbsolutePath().normalize().toString());
        command.add("--rule-json");
        command.add(ruleJsonPath.toString());
        command.add("--input");
        command.add(datasetPath.toString());
        command.add("--output-trades");
        command.add(outputTradesPath.toString());
        command.add("--rule-id");
        command.add(ruleId);
        command.add("--rule-name");
        command.add(ruleId);

        if (StringUtils.hasText(request.getPair())) {
            command.add("--pair");
            command.add(request.getPair().trim());
        }

        return command;
    }

    private int runPythonCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Rule executor failed with exitCode=" + exitCode + ". Python output: " + output
                );
            }

            log.info("Rule executor completed successfully. exitCode={}, output={}", exitCode, output);
            return exitCode;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start rule executor process: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Rule executor process was interrupted", e);
        }
    }
}
