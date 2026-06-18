# Rule Summary CSV Rule Name Validation Fix

## Root Cause

Rule summary generation validates the input trades CSV `rule_name` column before running the summary script. The validation only accepted the requested `ruleId`, but some generated trade CSV files store the human-readable `TradingRule.ruleName` in `rule_name`.

Rule 02 writes:

`GT-RULE-02 MACD EMA_STACK Full Trend 30/40 M5`

The requested API `ruleId` is:

`GT_RULE_02_MACD_EMA_FULL_TREND_30_40_M5_V1`

Because the validation only accepted the `ruleId`, a valid Rule 02 trade CSV was rejected even though the file path was correctly derived from `ruleId`.

## Files Changed

- `src/main/java/com/apeironsoft/GT_v1_orchestrator/rule_engine/service/RuleSummaryService.java`
- `src/test/java/com/apeironsoft/GT_v1_orchestrator/rule_engine/service/RuleSummaryServiceTest.java`
- `reports/rule-summary/STEP_FIX_RULE_SUMMARY_RULE_NAME_VALIDATION.md`

## Tests Added/Updated

- Added `generateSummaryAndSavePassesWhenCsvRuleNameEqualsRuleId`
- Added `generateSummaryAndSavePassesWhenCsvRuleNameEqualsResolvedRuleName`
- Added `generateSummaryAndSaveFailsWhenCsvRuleNameEqualsAnotherRuleName`
- Kept wrong input path validation coverage with `generateSummaryAndSaveFailsWhenInjectedInputPathDoesNotBelongToRequestedRule`
- Kept path-isolation coverage with `generateSummaryAndSaveUsesRequestedRuleDerivedTradeCsvForEachRule`

## Validation Result

`mvn test` completed successfully.

Result:

- Tests run: 6
- Failures: 0
- Errors: 0
- Skipped: 0
- Build: SUCCESS

## Final Status

PASS
