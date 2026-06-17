package com.apeironsoft.GT_v1_orchestrator.experiment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MergeTdTsRunResponse {
    private String status;
    private Integer exitCode;
    private String engineRootPath;
    private String scriptPath;
    private String firstFile;
    private String secondFile;
    private String outputCsvPath;
    private Long outputRowCount;
    private String stdout;
    private String stderr;
    private String errorMessage;
}
