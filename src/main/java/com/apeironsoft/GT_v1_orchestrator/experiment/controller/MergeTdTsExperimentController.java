package com.apeironsoft.GT_v1_orchestrator.experiment.controller;

import com.apeironsoft.GT_v1_orchestrator.experiment.dto.MergeTdTsRunRequest;
import com.apeironsoft.GT_v1_orchestrator.experiment.dto.MergeTdTsRunResponse;
import com.apeironsoft.GT_v1_orchestrator.experiment.service.MergeTdTsExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class MergeTdTsExperimentController {

    private final MergeTdTsExperimentService service;

    @PostMapping("${app.api.experiments.ema-macd}")
    public ResponseEntity<MergeTdTsRunResponse> run(@RequestBody MergeTdTsRunRequest request) {
        MergeTdTsRunResponse response = service.run(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.internalServerError().body(response);
    }
}
