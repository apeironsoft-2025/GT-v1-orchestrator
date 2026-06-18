package com.apeironsoft.GT_v1_orchestrator.indicator.service;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.IndicatorExecutionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndicatorScriptService {
    private final ResponseBuilder responseBuilder;

    public CommonResponse execute(IndicatorExecutionRequest req) {
        /**
         *   cd /d F:\GT-v1-engine
         *   python scripts\run_ema_stack_td_ts_from_cleaned_csv.py --file-name USDJPY_M5_cleaned.csv
         *   Replace USDJPY_M5_cleaned.csv with the cleaned CSV file name inside:
         *   F:\GT-v1-shared-storage\cleaned
         *   Optional full version:

         python F:\GT-v1-engine\scripts\run_ema_stack_td_ts_from_cleaned_csv.py
         --file-name USDJPY_M5_cleaned.csv
         --cleaned-root-path F:\GT-v1-shared-storage\cleaned
         --output-dir F:\GT-v1-shared-storage\indicators

         */

        try {
            List<String> command = new ArrayList<>();
            executionScriptPath(
                    req.getEngineRoot(),
                    req.getScriptRelativePath(),
                    command
            );

            log.info("LOG:: Executing Command: {}", command);

            Path inputCsvPath = Path.of(req.getInputFileRelativePath())
                    .toAbsolutePath()
                    .normalize();
            addCommandOption(command, "--cleaned-root-path", inputCsvPath.toString());
            log.info("LOG:: Executing Command: {}", command);

            String fileName = req.getFileName();
            addCommandOption(command, "--file-name", fileName);
            log.info("LOG:: Executing Command: {}", command);

            Path outputCsvPath = Path.of(req.getOutputFileRelativePath())
                    .toAbsolutePath()
                    .normalize();
            addCommandOption(command, "--output-dir", outputCsvPath.toString());
            log.info("LOG:: Executing Command: {}", command);

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            Process process = processBuilder.start();

            boolean finished = process.waitFor(300, TimeUnit.SECONDS);

            if (!finished) {
                throw new RuntimeException();
            }

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());
            int exitCode = process.exitValue();

            return responseBuilder.buildSuccessResponse("final command", stdout+"__"+stderr+"__"+exitCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void executionScriptPath(
            String engineRootPath,
            String scriptRelativePath,
            List<String> command
    ) {
        command.add("python");
        Path engineRoot = Path.of(engineRootPath)
                .toAbsolutePath()
                .normalize();
        log.info("LOG:: Engine Root Path: {}", engineRoot);
        Path scriptPath = engineRoot.resolve(scriptRelativePath)
                .toAbsolutePath()
                .normalize();
        log.info("LOG:: Script Path: {}", scriptPath);
        command.add(scriptPath.toString());
    }

    private void addCommandOption(List<String> command, String option, String value) {
        command.add(option);
        command.add(value);
    }

    private String readStream(java.io.InputStream inputStream) throws Exception {
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        return output.toString().trim();
    }

}

