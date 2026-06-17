package com.apeironsoft.GT_v1_orchestrator.rule_engine.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleExecuteRequest;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.model.RuleExecuteResponse;
import com.apeironsoft.GT_v1_orchestrator.rule_engine.service.RuleExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class RuleExecutionController {

    private final RuleExecutionService ruleExecutionService;
    private final ResponseBuilder responseBuilder;

    @PostMapping("${app.api.rule.execute}")
    public ResponseEntity<CommonResponse> executeRule(@RequestBody RuleExecuteRequest request) {
        RuleExecuteResponse response = ruleExecutionService.executeRule(request);
        return ResponseEntity.ok(responseBuilder.buildSuccessResponse(
                "rule executed and trade CSV generated",
                response
        ));
    }


}
