package com.apeironsoft.GT_v1_orchestrator.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component("commonResponseBuilder")
@RequiredArgsConstructor
@Slf4j
public class ResponseBuilder {
    public CommonResponse buildHandledErrorResponse(String message) {
        return buildHandledErrorResponse(message, null);
    }

    public CommonResponse buildHandledErrorResponse(String message, Object data) {
        return CommonResponse.builder()
                .isSuccess(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .statusMessage(message)
                .data(data)
                .build();
    }
    public CommonResponse buildSuccessResponse(String message, Object data) {
        return CommonResponse.builder()
                        .isSuccess(true)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage(message)
                        .data(data)
                        .build();

    }

    public CommonResponse buildSoftErrorResponse(String message, Object data) {
        return CommonResponse.builder()
                .isSuccess(false)
                .statusCode(HttpStatus.OK.value())
                .statusMessage(message)
                .data(data)
                .build();

    }

}
