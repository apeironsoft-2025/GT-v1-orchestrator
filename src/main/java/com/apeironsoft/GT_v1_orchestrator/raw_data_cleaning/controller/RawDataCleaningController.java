package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service.RawDataCleaningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RawDataCleaningController {
    private final RawDataCleaningService rawDataCleaningService;
    private final ResponseBuilder responseBuilder;

    @PostMapping("${app.api.raw-data-clean}")
    public ResponseEntity<CommonResponse> CleanRawData(@RequestParam("relativePath") String relativePath) {
        try {
            CommonResponse response = rawDataCleaningService.clean(relativePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("${app.api.raw-data-clean}")
    public ResponseEntity<CommonResponse> getCleanDataSet() {
        try {
            CommonResponse response = rawDataCleaningService.getCleanedData();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }

}
