package com.apeironsoft.GT_v1_orchestrator.rule_engine.repository;

import com.apeironsoft.GT_v1_orchestrator.rule_engine.entity.TradingRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TradingRuleRepository extends MongoRepository<TradingRule, String> {

    Optional<TradingRule> findByRuleId(String ruleId);

    boolean existsByRuleId(String ruleId);
}
