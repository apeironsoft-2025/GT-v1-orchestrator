package com.apeironsoft.GT_v1_orchestrator.rule_engine.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.TradingRule;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.CreateTradingRuleRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleExecuteRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.service.RuleCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class RuleCreationController {
    private final RuleCreationService ruleCreationService;
    private final ResponseBuilder responseBuilder;

    @PostMapping("${app.api.rule.create}")
    public ResponseEntity<CommonResponse> addRule(@RequestBody CreateTradingRuleRequest request) {
        TradingRule res = ruleCreationService.saveRule(request);
        return ResponseEntity.ok(responseBuilder.buildSuccessResponse("Success", res));
    }

    @GetMapping("${app.api.rule.get-all}")
    public ResponseEntity<CommonResponse> getAllRules() {
        List<TradingRule> res = ruleCreationService.getAllRules();
        return ResponseEntity.ok(responseBuilder.buildSuccessResponse("found all", res));
    }

    @GetMapping("${app.api.rule.get-one}")
    public ResponseEntity<CommonResponse> getOneRule(@RequestParam String ruleId) {
        TradingRule res = ruleCreationService.getRuleByRuleId(ruleId);
        return ResponseEntity.ok(responseBuilder.buildSuccessResponse("Success", res));
    }

}
