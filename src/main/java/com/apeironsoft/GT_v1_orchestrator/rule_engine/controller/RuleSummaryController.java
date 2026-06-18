package com.apeironsoft.GT_v1_orchestrator.rule_engine.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.RuleExecutionSummary;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleSummaryGenerateResponse;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.service.RuleSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class RuleSummaryController {

    private final RuleSummaryService ruleSummaryService;
    private final ResponseBuilder responseBuilder;

    @PostMapping("${app.api.rule.summary-generate}")
    public ResponseEntity<CommonResponse> generateSummaryAndSave(@RequestBody RuleSummaryGenerateRequest request) {
        try {
            RuleSummaryGenerateResponse response = ruleSummaryService.generateSummaryAndSave(request);
            return ResponseEntity.ok(responseBuilder.buildSuccessResponse("rule summary generated and saved", response));
        } catch (Exception e) {
            log.error("Failed to generate and save rule summary", e);
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("${app.api.rule.summary-get-one}/{ruleId}")
    public ResponseEntity<CommonResponse> getLatestSummaryByRuleId(@PathVariable String ruleId) {
        try {
            RuleExecutionSummary response = ruleSummaryService.getLatestSummaryByRuleId(ruleId);
            return ResponseEntity.ok(responseBuilder.buildSuccessResponse("rule summary found", response));
        } catch (Exception e) {
            log.error("Failed to get rule summary. ruleId={}", ruleId, e);
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("${app.api.rule.summary-get-all}")
    public ResponseEntity<CommonResponse> getAllSummaries() {
        try {
            List<RuleExecutionSummary> response = ruleSummaryService.getAllSummaries();
            return ResponseEntity.ok(responseBuilder.buildSuccessResponse("rule summaries found", response));
        } catch (Exception e) {
            log.error("Failed to get all rule summaries", e);
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }
}
