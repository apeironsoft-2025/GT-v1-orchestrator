package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IndicatorBacktestRequest {
    private String indicatorName;
    private String engineRoot;
    private String scriptPath;
    private String fileName;
    private String inputFilePath;
    private String outputFilePath;
}
