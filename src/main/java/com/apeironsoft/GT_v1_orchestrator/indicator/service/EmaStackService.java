package com.apeironsoft.GT_v1_orchestrator.indicator.service;

import com.apeironsoft.GT_v1_orchestrator.indicator.model.EmaStackRunResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.utils.PythonExperimentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmaStackService {

    private final PythonExperimentProperties properties;

    public EmaStackRunResponse run(String fileName) {
        Instant startedAt = Instant.now();
        Path engineRoot = Path.of(properties.getEngineRootPath()).toAbsolutePath().normalize();
        Path scriptPath = engineRoot.resolve(properties.getEmaStackScriptRelativePath()).toAbsolutePath().normalize();
        Path inputCsvPath = Path.of(properties.getCleanedRootPath()).resolve(fileName == null ? "" : fileName).toAbsolutePath().normalize();
        Path outputCsvPath = Path.of(properties.getEmaStackOutputDir()).resolve(toOutputFileName(fileName)).toAbsolutePath().normalize();

        try {
            validate(fileName, engineRoot, scriptPath, inputCsvPath);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    properties.getExecutable(),
                    scriptPath.toString(),
                    "--file-name",
                    fileName
            );
            processBuilder.directory(engineRoot.toFile());

            Process process = processBuilder.start();
            CompletableFuture<String> stdoutFuture = read(process.getInputStream());
            CompletableFuture<String> stderrFuture = read(process.getErrorStream());

            boolean completed = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return build("TIMEOUT", startedAt, null, engineRoot, scriptPath, fileName, inputCsvPath, outputCsvPath,
                        null, null, null, text(stdoutFuture), text(stderrFuture), "Python script timed out.");
            }

            String stdout = stdoutFuture.get(5, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(5, TimeUnit.SECONDS);
            int exitCode = process.exitValue();

            return build(
                    exitCode == 0 ? "SUCCESS" : "FAILED",
                    startedAt,
                    exitCode,
                    engineRoot,
                    scriptPath,
                    fileName,
                    inputCsvPath,
                    outputCsvPath,
                    countRows(outputCsvPath),
                    parseCounts(stdout, "EMA_STACK_TD count:", "EMA_STACK_TS count:"),
                    parseCounts(stdout, "EMA_STACK_TS count:", null),
                    stdout,
                    stderr,
                    exitCode == 0 ? null : "Python script failed with exit code " + exitCode
            );
        } catch (Exception ex) {
            return build("ERROR", startedAt, null, engineRoot, scriptPath, fileName, inputCsvPath, outputCsvPath,
                    null, null, null, null, null, ex.getMessage());
        }
    }

    private void validate(String fileName, Path engineRoot, Path scriptPath, Path inputCsvPath) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("fileName must be a file name only.");
        }
        if (!Files.exists(engineRoot)) {
            throw new IllegalStateException("Engine root does not exist: " + engineRoot);
        }
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("EMA Stack script does not exist: " + scriptPath);
        }
        if (!Files.exists(inputCsvPath) || !Files.isRegularFile(inputCsvPath)) {
            throw new IllegalStateException("Input CSV does not exist: " + inputCsvPath);
        }
    }

    private CompletableFuture<String> read(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception ex) {
                return ex.getMessage();
            }
        });
    }

    private String text(CompletableFuture<String> future) {
        return future.getNow("");
    }

    private Long countRows(Path csvPath) throws Exception {
        if (!Files.exists(csvPath)) {
            return null;
        }
        try (var lines = Files.lines(csvPath)) {
            return Math.max(0, lines.count() - 1);
        }
    }

    private Map<String, Long> parseCounts(String stdout, String startMarker, String endMarker) {
        Map<String, Long> counts = new LinkedHashMap<>();
        int start = stdout == null ? -1 : stdout.indexOf(startMarker);
        if (start < 0) {
            return counts;
        }

        int contentStart = start + startMarker.length();
        int contentEnd = endMarker == null ? stdout.length() : stdout.indexOf(endMarker, contentStart);
        String block = stdout.substring(contentStart, contentEnd < 0 ? stdout.length() : contentEnd);

        for (String line : block.split("\\R")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                try {
                    counts.put(parts[0], Long.parseLong(parts[parts.length - 1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return counts;
    }

    private String toOutputFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (fileName.endsWith("_cleaned.csv")) {
            return fileName.replace("_cleaned.csv", "_ema_stack_td_ts.csv");
        }
        return fileName.replace(".csv", "_ema_stack_td_ts.csv");
    }

    private EmaStackRunResponse build(
            String status,
            Instant startedAt,
            Integer exitCode,
            Path engineRoot,
            Path scriptPath,
            String fileName,
            Path inputCsvPath,
            Path outputCsvPath,
            Long outputRowCount,
            Map<String, Long> tdCounts,
            Map<String, Long> tsCounts,
            String stdout,
            String stderr,
            String errorMessage
    ) {
        return EmaStackRunResponse.builder()
                .status(status)
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .exitCode(exitCode)
                .engineRootPath(engineRoot.toString())
                .scriptPath(scriptPath.toString())
                .inputFileName(fileName)
                .inputCsvPath(inputCsvPath.toString())
                .outputCsvPath(outputCsvPath.toString())
                .outputRowCount(outputRowCount)
                .emaStackTdCounts(tdCounts)
                .emaStackTsCounts(tsCounts)
                .stdout(stdout)
                .stderr(stderr)
                .errorMessage(errorMessage)
                .build();
    }
}
