package com.apeironsoft.GT_v1_orchestrator.experiment.service;

import com.apeironsoft.GT_v1_orchestrator.experiment.dto.MergeTdTsRunRequest;
import com.apeironsoft.GT_v1_orchestrator.experiment.dto.MergeTdTsRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.utils.PythonExperimentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MergeTdTsExperimentService {

    private final PythonExperimentProperties properties;

    public MergeTdTsRunResponse run(MergeTdTsRunRequest request) {
        Path engineRoot = pathOrEmpty(properties.getEngineRootPath()).toAbsolutePath().normalize();
        Path scriptPath = engineRoot.resolve(valueOrEmpty(properties.getMergeTdTsScriptRelativePath())).toAbsolutePath().normalize();
        Path indicatorsRoot = pathOrEmpty(properties.getIndicatorsRootPath()).toAbsolutePath().normalize();
        Path firstCsvPath = indicatorsRoot.resolve(request == null ? "" : valueOrEmpty(request.getFirstFile())).toAbsolutePath().normalize();
        Path secondCsvPath = indicatorsRoot.resolve(request == null ? "" : valueOrEmpty(request.getSecondFile())).toAbsolutePath().normalize();

        String stdout = null;
        String stderr = null;

        try {
            validateRequest(request);
            validatePaths(engineRoot, scriptPath, firstCsvPath, secondCsvPath);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    properties.getExecutable(),
                    scriptPath.toString(),
                    "--first-file",
                    request.getFirstFile(),
                    "--second-file",
                    request.getSecondFile(),
                    "--indicators-root",
                    properties.getIndicatorsRootPath(),
                    "--experiments-root",
                    properties.getExperimentsRootPath()
            );

            processBuilder.directory(engineRoot.toFile());
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();
            boolean completed = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

                return build("TIMEOUT", null, engineRoot, scriptPath, request, null, null, stdout, stderr,
                        "Python script timed out.");
            }

            stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.exitValue();

            String outputCsvPath = parseOutputCsvPath(stdout);
            Long outputRowCount = countRows(outputCsvPath);

            return build(
                    exitCode == 0 ? "SUCCESS" : "FAILED",
                    exitCode,
                    engineRoot,
                    scriptPath,
                    request,
                    outputCsvPath,
                    outputRowCount,
                    stdout,
                    stderr,
                    exitCode == 0 ? null : "Python script failed with exit code " + exitCode
            );
        } catch (Exception ex) {
            return build("ERROR", null, engineRoot, scriptPath, request, null, null, stdout, stderr, ex.getMessage());
        }
    }

    private void validateRequest(MergeTdTsRunRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        validateFile(request.getFirstFile(), "firstFile");
        validateFile(request.getSecondFile(), "secondFile");
    }

    private void validateFile(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (!value.endsWith(".csv")) {
            throw new IllegalArgumentException(fieldName + " must end with .csv.");
        }
        if (value.contains("..")) {
            throw new IllegalArgumentException(fieldName + " must not contain '..'.");
        }
        if (Path.of(value).isAbsolute()) {
            throw new IllegalArgumentException(fieldName + " must not be an absolute path.");
        }
    }

    private void validatePaths(Path engineRoot, Path scriptPath, Path firstCsvPath, Path secondCsvPath) {
        if (!Files.exists(engineRoot)) {
            throw new IllegalStateException("Engine root does not exist: " + engineRoot);
        }
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Merge TD/TS script does not exist: " + scriptPath);
        }
        if (!Files.exists(firstCsvPath) || !Files.isRegularFile(firstCsvPath)) {
            throw new IllegalStateException("First input CSV does not exist: " + firstCsvPath);
        }
        if (!Files.exists(secondCsvPath) || !Files.isRegularFile(secondCsvPath)) {
            throw new IllegalStateException("Second input CSV does not exist: " + secondCsvPath);
        }
    }

    private String parseOutputCsvPath(String stdout) {
        if (stdout == null) {
            return null;
        }
        for (String line : stdout.split("\\R")) {
            if (line.startsWith("Output CSV:")) {
                return line.substring("Output CSV:".length()).trim();
            }
        }
        return null;
    }

    private Long countRows(String outputCsvPath) throws Exception {
        if (outputCsvPath == null || outputCsvPath.isBlank()) {
            return null;
        }
        Path path = Path.of(outputCsvPath);
        if (!Files.exists(path)) {
            return null;
        }
        try (var lines = Files.lines(path)) {
            return Math.max(0, lines.count() - 1);
        }
    }

    private MergeTdTsRunResponse build(
            String status,
            Integer exitCode,
            Path engineRoot,
            Path scriptPath,
            MergeTdTsRunRequest request,
            String outputCsvPath,
            Long outputRowCount,
            String stdout,
            String stderr,
            String errorMessage
    ) {
        return MergeTdTsRunResponse.builder()
                .status(status)
                .exitCode(exitCode)
                .engineRootPath(engineRoot.toString())
                .scriptPath(scriptPath.toString())
                .firstFile(request == null ? null : request.getFirstFile())
                .secondFile(request == null ? null : request.getSecondFile())
                .outputCsvPath(outputCsvPath)
                .outputRowCount(outputRowCount)
                .stdout(stdout)
                .stderr(stderr)
                .errorMessage(errorMessage)
                .build();
    }

    private Path pathOrEmpty(String value) {
        return Path.of(valueOrEmpty(value));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
