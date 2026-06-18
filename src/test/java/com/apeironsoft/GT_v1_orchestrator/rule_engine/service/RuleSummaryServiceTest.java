package com.apeironsoft.GT_v1_orchestrator.rule_engine.service;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.RuleExecutionSummary;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.TradingRule;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateResponse;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.repository.RuleExecutionSummaryRepository;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.repository.TradingRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSummaryServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private RuleExecutionSummaryRepository ruleExecutionSummaryRepository;

    @Mock
    private TradingRuleRepository tradingRuleRepository;

    private RuleSummaryService ruleSummaryService;
    private Path ruleOutputDir;

    @BeforeEach
    void setUp() throws Exception {
        ruleOutputDir = tempDir.resolve("rule-output");
        Files.createDirectories(ruleOutputDir);

        ruleSummaryService = new RuleSummaryService(
                ruleExecutionSummaryRepository,
                tradingRuleRepository,
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(ruleSummaryService, "ruleOutputDir", ruleOutputDir.toString());
        ReflectionTestUtils.setField(ruleSummaryService, "summaryScriptPath", writeSummaryScript().toString());

    }

    @Test
    void generateSummaryAndSaveUsesRequestedRuleDerivedTradeCsvForEachRule() throws Exception {
        writeTradeCsv("GT_RULE_TEST_A", 4.0, 6.0);
        writeTradeCsv("GT_RULE_TEST_B", 100.0, 50.0);
        when(tradingRuleRepository.findByRuleId("GT_RULE_TEST_A"))
                .thenReturn(Optional.of(tradingRule("GT_RULE_TEST_A", "Rule Test A")));
        when(tradingRuleRepository.findByRuleId("GT_RULE_TEST_B"))
                .thenReturn(Optional.of(tradingRule("GT_RULE_TEST_B", "Rule Test B")));
        when(ruleExecutionSummaryRepository.save(any(RuleExecutionSummary.class)))
                .thenAnswer(invocation -> {
                    RuleExecutionSummary summary = invocation.getArgument(0);
                    summary.setId("summary-" + summary.getRuleId());
                    return summary;
                });

        RuleSummaryGenerateRequest requestA = request("GT_RULE_TEST_A", "GT_RULE_TEST_B_trades.csv", "Stale B Name");
        RuleSummaryGenerateRequest requestB = request("GT_RULE_TEST_B", "GT_RULE_TEST_A_trades.csv", "Stale A Name");

        RuleSummaryGenerateResponse responseA = ruleSummaryService.generateSummaryAndSave(requestA);
        RuleSummaryGenerateResponse responseB = ruleSummaryService.generateSummaryAndSave(requestB);

        Map<String, Object> summaryA = readSummary("GT_RULE_TEST_A");
        Map<String, Object> summaryB = readSummary("GT_RULE_TEST_B");
        String pathA = ruleOutputDir.resolve("GT_RULE_TEST_A_trades.csv").toAbsolutePath().normalize().toString();
        String pathB = ruleOutputDir.resolve("GT_RULE_TEST_B_trades.csv").toAbsolutePath().normalize().toString();

        assertThat(responseA.getInputTradesPath()).isEqualTo(pathA);
        assertThat(responseB.getInputTradesPath()).isEqualTo(pathB);
        assertThat(summaryA.get("inputTradesPath")).isEqualTo(pathA);
        assertThat(summaryB.get("inputTradesPath")).isEqualTo(pathB);
        assertThat(summaryA.get("ruleId")).isEqualTo("GT_RULE_TEST_A");
        assertThat(summaryB.get("ruleId")).isEqualTo("GT_RULE_TEST_B");
        assertThat(summaryA.get("ruleName")).isEqualTo("Rule Test A");
        assertThat(summaryB.get("ruleName")).isEqualTo("Rule Test B");
        assertThat(summaryA.get("netPips")).isEqualTo(10.0);
        assertThat(summaryB.get("netPips")).isEqualTo(150.0);
        assertThat(summaryB.get("inputTradesPath")).isNotEqualTo(summaryA.get("inputTradesPath"));
        assertThat(summaryB.get("netPips")).isNotEqualTo(summaryA.get("netPips"));
    }

    @Test
    void generateSummaryAndSaveFailsWhenInjectedInputPathDoesNotBelongToRequestedRule() throws Exception {
        Path pathA = writeTradeCsv("GT_RULE_TEST_A", 4.0, 6.0);
        writeTradeCsv("GT_RULE_TEST_B", 100.0, 50.0);

        assertThatThrownBy(() -> ruleSummaryService.generateSummaryAndSave(request("GT_RULE_TEST_B", null, null), pathA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input trades path filename must contain requested ruleId")
                .hasMessageContaining("GT_RULE_TEST_B")
                .hasMessageContaining("GT_RULE_TEST_A_trades.csv");
    }

    private RuleSummaryGenerateRequest request(String ruleId, String inputTradesPath, String ruleName) {
        RuleSummaryGenerateRequest request = new RuleSummaryGenerateRequest();
        request.setRuleId(ruleId);
        request.setInputTradesPath(inputTradesPath);
        request.setRuleName(ruleName);
        return request;
    }

    private TradingRule tradingRule(String ruleId, String ruleName) {
        return TradingRule.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .build();
    }

    private Path writeTradeCsv(String ruleId, double firstPips, double secondPips) throws Exception {
        Path path = ruleOutputDir.resolve(ruleId + "_trades.csv");
        Files.writeString(path, """
                rule_name,direction,entry_datetime,close_datetime,close_status,collected_pips,cc
                %s,UP,2026-01-05 01:00:00,2026-01-05 01:05:00,TAKE_PROFIT,%s,1
                %s,DOWN,2026-01-05 07:00:00,2026-01-05 07:05:00,TAKE_PROFIT,%s,2
                """.formatted(ruleId, firstPips, ruleId, secondPips));
        return path;
    }

    private Path writeSummaryScript() throws Exception {
        Path path = tempDir.resolve("summary_script.py");
        Files.writeString(path, """
                import argparse
                import csv
                import json
                from pathlib import Path

                parser = argparse.ArgumentParser()
                parser.add_argument("--input-trades", required=True)
                parser.add_argument("--output-summary", required=True)
                parser.add_argument("--rule-id", required=True)
                parser.add_argument("--rule-name", required=True)
                args = parser.parse_args()

                with open(args.input_trades, newline="", encoding="utf-8-sig") as handle:
                    rows = list(csv.DictReader(handle))

                summary = {
                    "ruleId": args.rule_id,
                    "ruleName": args.rule_name,
                    "inputTradesPath": str(Path(args.input_trades).resolve()),
                    "totalTrades": len(rows),
                    "netPips": round(sum(float(row["collected_pips"]) for row in rows), 3),
                }

                output = Path(args.output_summary)
                output.parent.mkdir(parents=True, exist_ok=True)
                output.write_text(json.dumps(summary), encoding="utf-8")
                """);
        return path;
    }

    private Map<String, Object> readSummary(String ruleId) throws Exception {
        return new ObjectMapper().readValue(
                ruleOutputDir.resolve(ruleId + "_summary.json").toFile(),
                Map.class
        );
    }
}
