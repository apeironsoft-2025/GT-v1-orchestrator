package com.apeironsoft.GT_v1_orchestrator.indicator.controller;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.EmaStackRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.IndicatorBacktestRequest;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.IndicatorExecutionRequest;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.MacdRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.EmaStackService;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.IndicatorFilesService;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.IndicatorScriptService;
import com.apeironsoft.GT_v1_orchestrator.indicator.service.MacdExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class IndicatorScriptController {
    private final EmaStackService emaStackService;
    private final IndicatorFilesService indicatorFilesService;
    private final MacdExperimentService macdService;
    private final ResponseBuilder responseBuilder;
    private final IndicatorScriptService scriptService;

    @PostMapping("/api/v1/indicator-script")
    public ResponseEntity<CommonResponse> execute(@RequestBody IndicatorExecutionRequest executionRequest){
        log.info("IndicatorScriptController.execute: executionRequest={}", executionRequest);
        CommonResponse response = scriptService.execute(executionRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/indicator-backtest")
    public ResponseEntity<CommonResponse> backTestOnIndicator(@RequestBody IndicatorBacktestRequest backtestRequest){
        log.info("IndicatorScriptController.backTestOnIndicator");
        CommonResponse response = scriptService.runBacktest(backtestRequest);
        return new  ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("${app.api.indicator.execution}")
    public ResponseEntity<CommonResponse> run(@RequestBody IndicatorExecutionRequest executionRequest, HttpRequest httpRequest) throws IOException {
        log.info("LOG:: uri={}, body={}",httpRequest.getURI(), executionRequest);
        CommonResponse commonResponse = null;
//        if(type.equalsIgnoreCase("macd")) {
//           MacdRunResponse macdRunResponse = macdService.run(fileName);
//            if ("SUCCESS".equals(macdRunResponse.getStatus())) {
//              commonResponse = responseBuilder.buildSuccessResponse("Success", macdRunResponse);
//            }else{
//                commonResponse = responseBuilder.buildHandledErrorResponse("Error", macdRunResponse);
//            }
//        }else if(type.equalsIgnoreCase("ema")) {
//            EmaStackRunResponse emaStackRunResponse = emaStackService.run(fileName);
//
//            if ("SUCCESS".equals(emaStackRunResponse.getStatus())) {
//               commonResponse = responseBuilder.buildSuccessResponse("Success", emaStackRunResponse);
//            }else {
//                commonResponse = responseBuilder.buildHandledErrorResponse("Error", emaStackRunResponse);
//            }
//        }
        return ResponseEntity.ok(commonResponse);
    }

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


}
