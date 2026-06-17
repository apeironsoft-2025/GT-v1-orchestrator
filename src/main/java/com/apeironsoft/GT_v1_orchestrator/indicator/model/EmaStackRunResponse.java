package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class EmaStackRunResponse {
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    private Integer exitCode;

    private String engineRootPath;
    private String scriptPath;
    private String inputFileName;
    private String inputCsvPath;
    private String outputCsvPath;

    private Long outputRowCount;
    private Map<String, Long> emaStackTdCounts;
    private Map<String, Long> emaStackTsCounts;

    private String stdout;
    private String stderr;
    private String errorMessage;
}
