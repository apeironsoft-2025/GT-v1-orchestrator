package com.apeironsoft.GT_v1_orchestrator.indicator.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.EmaStackRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.MacdRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.EmaStackService;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.IndicatorFilesService;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.MacdExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class IndicatorController {
    private final EmaStackService emaStackService;
    private final IndicatorFilesService indicatorFilesService;
    private final MacdExperimentService service;
    private final ResponseBuilder responseBuilder;

    @GetMapping("${app.api.indicator.details}")
    public ResponseEntity<CommonResponse> getIndicatorFiles() {
        try {
            return ResponseEntity.ok(responseBuilder.buildSuccessResponse(
                    "Success",
                    indicatorFilesService.getIndicatorFiles()
            ));
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("${app.api.indicator.run}")
    public ResponseEntity<CommonResponse> run(
            @RequestParam("fileName") String fileName,
            @RequestParam("type") String type // MACD, EMA, ADX
    ) {
        CommonResponse commonResponse = null;
        if(type.equalsIgnoreCase("macd")) {
           MacdRunResponse macdRunResponse = service.run(fileName);
            if ("SUCCESS".equals(macdRunResponse.getStatus())) {
              commonResponse = responseBuilder.buildSuccessResponse("Success", macdRunResponse);
            }else{
                commonResponse = responseBuilder.buildHandledErrorResponse("Error", macdRunResponse);
            }
        }else if(type.equalsIgnoreCase("ema")) {
            EmaStackRunResponse emaStackRunResponse = emaStackService.run(fileName);

            if ("SUCCESS".equals(emaStackRunResponse.getStatus())) {
               commonResponse = responseBuilder.buildSuccessResponse("Success", emaStackRunResponse);
            }else {
                commonResponse = responseBuilder.buildHandledErrorResponse("Error", emaStackRunResponse);
            }
        }
        return ResponseEntity.ok(commonResponse);
    }

}
