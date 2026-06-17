package com.apeironsoft.GT_v1_orchestrator.indicator.service;

import com.apeironsoft.GT_v1_orchestrator.indicator.model.MacdRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.utils.PythonExperimentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MacdExperimentService {

    private final PythonExperimentProperties properties;

    @Value("${app.storage.cleaned-csv-dir}")
    private String cleanedCsvDir;

    public MacdRunResponse run(String fileName) {
        Instant startedAt = Instant.now();

        Path engineRoot = Path.of(properties.getEngineRootPath()).toAbsolutePath().normalize();
        Path scriptPath = engineRoot.resolve(properties.getMacdScriptRelativePath()).toAbsolutePath().normalize();
        Path inputCsvPath = Path.of(cleanedCsvDir).resolve(fileName).toAbsolutePath().normalize();
        Path outputDir = Path.of(properties.getOutputDir()).toAbsolutePath().normalize();
        Path outputCsvPath = outputDir.resolve(toMacdOutputFileName(fileName));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    properties.getExecutable(),
                    scriptPath.toString(),
                    "--input-csv",
                    inputCsvPath.toString(),
                    "--output-dir",
                    outputDir.toString()
            );

            processBuilder.directory(engineRoot.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            return MacdRunResponse.builder()
                    .status(exitCode == 0 ? "SUCCESS" : "FAILED")
                    .startedAt(startedAt)
                    .finishedAt(Instant.now())
                    .exitCode(exitCode)
                    .engineRootPath(engineRoot.toString())
                    .scriptPath(scriptPath.toString())
                    .inputCsvPath(inputCsvPath.toString())
                    .outputDir(outputDir.toString())
                    .outputCsvPath(outputCsvPath.toString())
                    .stdout(output)
                    .errorMessage(exitCode == 0 ? null : "Python script failed with exit code " + exitCode)
                    .build();

        } catch (Exception ex) {
            return MacdRunResponse.builder()
                    .status("ERROR")
                    .startedAt(startedAt)
                    .finishedAt(Instant.now())
                    .engineRootPath(engineRoot.toString())
                    .scriptPath(scriptPath.toString())
                    .inputCsvPath(inputCsvPath.toString())
                    .outputDir(outputDir.toString())
                    .outputCsvPath(outputCsvPath.toString())
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    private String toMacdOutputFileName(String fileName) {
        String stem = fileName.endsWith(".csv") ? fileName.substring(0, fileName.length() - 4) : fileName;
        stem = stem.endsWith("_cleaned") ? stem.substring(0, stem.length() - "_cleaned".length()) : stem;
        return stem + "_macd_td_ts.csv";
    }
}
