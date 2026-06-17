package com.apeironsoft.GT_v1_orchestrator.indicator.controller;

import com.apeironsoft.GT_v1_orchestrator.indicator.model.MacdRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.MacdExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MacdController {
    private final MacdExperimentService service;

    @PostMapping("${app.api.indicator.macd-run}")
    public ResponseEntity<MacdRunResponse> runMacdExperiment(
            @RequestParam("fileName") String fileName
    ) {
        MacdRunResponse response = service.run(fileName);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.internalServerError().body(response);
    }
}
