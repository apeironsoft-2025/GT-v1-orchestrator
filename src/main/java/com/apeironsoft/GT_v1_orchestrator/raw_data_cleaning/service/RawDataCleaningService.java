package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.raw_file_handler.model.LocalFileListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RawDataCleaningService {

    @Value("${app.storage.shared-root}")
    private String sharedRoot;
    @Value("${app.storage.cleaned-csv-dir}")
    private String cleanedCsvDir;
    private final ResponseBuilder responseBuilder;

    public CommonResponse clean(String relativePath) {
        log.info("Start to clean raw data for path {}", relativePath);

        if (relativePath == null || relativePath.isBlank()) {
            return responseBuilder.buildHandledErrorResponse("relativePath is required");
        }

        String normalizedRelativePath = Path.of(relativePath).normalize().toString();
        if (normalizedRelativePath.startsWith("..") || Path.of(normalizedRelativePath).isAbsolute()) {
            return responseBuilder.buildHandledErrorResponse("Invalid relativePath=" + relativePath);
        }

        String fileBaseName = normalizedRelativePath.replaceFirst("(?i)\\.csv$", "");
        String outputPath = Path.of(sharedRoot, "cleaned", fileBaseName + "_cleaned.csv").toString();
        String summaryPath = Path.of(sharedRoot, "reports", fileBaseName + "_cleaning_summary.json").toString();

        try {
            Process process = getProcess(fileBaseName, outputPath, summaryPath);

            String output = new String(process.getInputStream().readAllBytes());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String summary = readFileIfExists(summaryPath);
                log.error(
                        "Error while cleaning raw data for path {}. exitCode={}, output={}, summary={}",
                        normalizedRelativePath,
                        exitCode,
                        output,
                        summary
                );
                return responseBuilder.buildHandledErrorResponse(
                        "Error while cleaning raw data for path=" + normalizedRelativePath,
                        Map.of(
                                "exitCode", exitCode,
                                "output", output,
                                "summaryPath", summaryPath,
                                "summary", summary
                        )
                );
            }

            System.out.println("Python cleaner success: " + output);
            log.info("Python cleaner success: {}", output);
            return responseBuilder.buildSuccessResponse("Success", output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error while cleaning raw data for path {}", normalizedRelativePath, e);
            return responseBuilder.buildHandledErrorResponse("Error while cleaning raw data for path=" + normalizedRelativePath);
        } catch (IOException | RuntimeException e) {
            log.error("Error while cleaning raw data for path {}", normalizedRelativePath, e);
            return responseBuilder.buildHandledErrorResponse("Error while cleaning raw data for path=" + normalizedRelativePath);
        }
    }

    private Process getProcess(String fileBaseName, String outputPath, String summaryPath) {
        String inputPath = Path.of(sharedRoot, "raw", fileBaseName + ".csv").toString();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "-m", "gt_v1_engine.clean_csv",
                    "--input", inputPath,
                    "--output", outputPath,
                    "--summary", summaryPath
            );

            pb.directory(new File("F:\\GT-v1-engine"));
            pb.redirectErrorStream(true);
            log.info("LOG:: command executed: {}", pb.command());
            return pb.start();
        } catch (IOException e) {
            log.error("LOG:: Error while cleaning raw data for path {}", fileBaseName, e);
            throw new RuntimeException(e);
        }
    }

    private String readFileIfExists(String path) {
        try {
            Path resolvedPath = Path.of(path);
            return Files.exists(resolvedPath) ? Files.readString(resolvedPath) : "";
        } catch (IOException e) {
            log.warn("Unable to read cleaning summary {}", path, e);
            return "";
        }
    }

    public CommonResponse getCleanedData() {
        Path rootPath = Paths.get(cleanedCsvDir).toAbsolutePath().normalize();

        if (!Files.exists(rootPath)) {

            return responseBuilder.buildSoftErrorResponse(
                    "files not found",
                    new LocalFileListResponse(
                            0,
                            rootPath.toString(),
                            List.of()
                    )
            );

        }

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            List<LocalFileListResponse.LocalFileInfo> files = pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> toFileInfo(rootPath, path))
                    .sorted(Comparator.comparing(LocalFileListResponse.LocalFileInfo::lastModifiedAt).reversed())
                    .toList();

            return responseBuilder.buildSuccessResponse(
                    "Files found",
                    new LocalFileListResponse(
                            files.size(),
                            rootPath.toString(),
                            files
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalFileListResponse.LocalFileInfo toFileInfo(Path rootPath, Path filePath) {
        try {
            Path absolutePath = filePath.toAbsolutePath().normalize();

            return new LocalFileListResponse.LocalFileInfo(
                    filePath.getFileName().toString(),
                    rootPath.relativize(absolutePath).toString(),
                    absolutePath.toString(),
                    Files.size(filePath),
                    Files.getLastModifiedTime(filePath).toInstant()
            );
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file info: " + filePath, ex);
        }
    }

}
