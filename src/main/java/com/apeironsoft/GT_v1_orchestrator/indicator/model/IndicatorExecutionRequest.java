package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IndicatorExecutionRequest {
    private String indicatorName;
    private String engineRoot;
    private String scriptRelativePath;
    private String fileName;
    private String inputFileRelativePath;
    private String outputFileRelativePath;
}
