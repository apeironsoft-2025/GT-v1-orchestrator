package com.apeironsoft.GT_v1_orchestrator.indicator.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IndicatorFilesResponse {
    private String description;
    private String indicatorsRootPath;
    private List<String> experimentInputFileNames;
    private List<IndicatorFileInfo> files;
}
