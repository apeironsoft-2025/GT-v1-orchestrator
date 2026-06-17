package com.apeironsoft.GT_v1_orchestrator.indicator.controller;

import com.apeironsoft.GT_v1_orchestrator.indicator.model.EmaStackRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.EmaStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class EmaStackController {
    private final EmaStackService service;

    @PostMapping("${app.api.indicator.ema-stack-run}")
    public ResponseEntity<EmaStackRunResponse> run(@RequestParam("fileName") String fileName) {
        EmaStackRunResponse response = service.run(fileName);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.internalServerError().body(response);
    }
}
