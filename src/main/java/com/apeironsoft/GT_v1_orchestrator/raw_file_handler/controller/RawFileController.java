package com.apeironsoft.GT_v1_orchestrator.raw_file_handler.controller;

import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.raw_file_handler.service.RawFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class RawFileController {

    private final ResponseBuilder responseBuilder;
    private final RawFileService rawFileService;

    @PostMapping(
            value = "${app.api.raw-file}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommonResponse> saveFileToSharedVolume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName
            ) {
        log.info("LOG:: uploadFile api called");
        // Validation --------------
        if (file.isEmpty() || fileName == null || fileName.isEmpty()) {
            log.info("LOG:: file is empty");
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse("File is empty or file name is empty"));
        }
        log.info("LOG:: file received");
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "input.csv" : file.getOriginalFilename()
        );

        if (!originalFilename.toLowerCase().endsWith(".csv")) {
            log.info("LOG:: file extension not supported");
            return ResponseEntity.ok(responseBuilder.buildHandledErrorResponse("File is not a CSV file"));
        }

        CommonResponse response = rawFileService.saveToLocalAndCreateLog(file, fileName);

        return ResponseEntity.ok(response);
    }

    @GetMapping("${app.api.raw-file}")
    public ResponseEntity<CommonResponse> getFileDetailsFromSharedVolume() {
        try {
            return ResponseEntity.ok(rawFileService.getSavedLocalFiles());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("${app.api.raw-file}")
    public ResponseEntity<CommonResponse> deleteFilesFromSharedVolume(
            @RequestParam("relativePath") String relativePath
    ) {
        try {
            log.info("LOG:: deleteRawLocalFile called");
            return ResponseEntity.ok(rawFileService.deleteRawLocalFile(relativePath));
        } catch (IOException e) {
            log.error("LOG:: delete file from local is error, {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}