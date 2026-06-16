package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service.CleanedDataService;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service.RawDataCleaningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CleanedDataController {

    private final CleanedDataService cleanedDataService;
    private final ResponseBuilder responseBuilder;

    @GetMapping("${app.api.cleaned-data-details}")
    public ResponseEntity<CommonResponse> getCleanedDataDetails(@RequestParam("relativePath") String relativePath) {
        try {
            CommonResponse response = cleanedDataService.getCleanedFileDetails(relativePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }
}
