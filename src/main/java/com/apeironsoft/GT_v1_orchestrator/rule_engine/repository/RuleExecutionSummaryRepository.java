package com.apeironsoft.GT_v1_orchestrator.rule_engine.repository;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.RuleExecutionSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RuleExecutionSummaryRepository extends MongoRepository<RuleExecutionSummary, String> {

    List<RuleExecutionSummary> findByRuleIdOrderByCreatedAtDesc(String ruleId);

    Optional<RuleExecutionSummary> findFirstByRuleIdOrderByCreatedAtDesc(String ruleId);
}
