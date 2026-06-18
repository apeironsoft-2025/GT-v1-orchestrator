# Step Fix Rule Summary Path Bug

## Root Cause Found

`RuleSummaryService.generateSummaryAndSave` accepted `inputTradesPath` from the request payload and used it directly when running the summary script and saving the DB record. If a client reused a stale request body or previous UI state, a summary request for one `ruleId` could summarize another rule's `<ruleId>_trades.csv`.

The service also allowed request-provided `ruleName`, which meant stale request data could copy a previous rule name into the generated summary.

## Files Changed

- `src/main/java/com/apeironsoft/GT_v1_orchestrator/rule_engine/service/RuleSummaryService.java`
- `src/test/java/com/apeironsoft/GT_v1_orchestrator/rule_engine/service/RuleSummaryServiceTest.java`
- `reports/rule-summary/STEP_FIX_RULE_SUMMARY_PATH_BUG.md`

## Tests Added/Updated

- Added `RuleSummaryServiceTest.generateSummaryAndSaveUsesRequestedRuleDerivedTradeCsvForEachRule`
  - Creates `GT_RULE_TEST_A_trades.csv` and `GT_RULE_TEST_B_trades.csv`.
  - Generates summaries for A and B.
  - Verifies each summary uses its own derived input path and net pips.
  - Verifies B does not contain A's path or net pips.
- Added `RuleSummaryServiceTest.generateSummaryAndSaveFailsWhenInjectedInputPathDoesNotBelongToRequestedRule`
  - Requests `GT_RULE_TEST_B` with `GT_RULE_TEST_A_trades.csv`.
  - Verifies generation fails before summary persistence.

## Validation Commands Run

- `mvn test`

Result:

- Tests run: 3
- Failures: 0
- Errors: 0
- Build: SUCCESS

## Before Expected Behavior

Before the fix, the backend could summarize a requested `ruleId` using a stale request `inputTradesPath`, so `GT_RULE_01_MACD_EMA_STACK_42_38_M5_V1_summary.json` could be generated from `GT_RULE_01_MACD_EMA_STACK_32_28_M5_V1_trades.csv`.

## After Expected Behavior

For every public summary request:

- `inputTradesPath` is derived as `F:\GT-v1-shared-storage\rule-output\<ruleId>_trades.csv`.
- `summaryJsonPath` is derived as `F:\GT-v1-shared-storage\rule-output\<ruleId>_summary.json`.
- `summaryData.ruleId` must match the requested `ruleId`.
- `summaryData.ruleName` comes from the requested rule document, falling back to `ruleId`.
- `summaryData.inputTradesPath` must match the derived input path.
- The DB summary record stores the same derived input and summary paths.
- Generation fails if the input filename does not contain the requested `ruleId`.
- Generation fails if a CSV `rule_name` column contains values different from the requested `ruleId`.

## Final Status

PASS
