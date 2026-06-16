package com.apeironsoft.GT_v1_orchestrator.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class CommonResponse {
    private Boolean isSuccess;
    private Integer statusCode;
    private String statusMessage;
    private Object data;
}
