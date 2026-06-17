package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class IndicatorFileInfo {
    private String fileName;
    private String experimentInputFileName;
    private String relativePath;
    private Long sizeBytes;
    private Instant lastModifiedAt;
}
