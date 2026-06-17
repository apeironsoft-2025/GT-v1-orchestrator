package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MacdRunResponse {

    private String status;

    private Instant startedAt;
    private Instant finishedAt;

    private Integer exitCode;

    private String engineRootPath;
    private String scriptPath;
    private String inputCsvPath;
    private String outputDir;
    private String outputCsvPath;

    private String stdout;
    private String errorMessage;
}
