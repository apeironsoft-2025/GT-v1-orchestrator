package com.apeironsoft.GT_v1_orchestrator.experiment.dto;

import lombok.Data;

@Data
public class MergeTdTsRunRequest {
    private String firstFile;
    private String secondFile;
}
