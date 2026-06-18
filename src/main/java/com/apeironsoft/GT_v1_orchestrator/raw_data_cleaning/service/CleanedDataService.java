package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service;

import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.entity.CleanedCsvDetailsLog;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.model.CleanedCsvDetailsJsonDto;
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
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanedDataService {
    @Value("${app.storage.cleaned-csv-dir}")
    private String cleanedCsvDir;
    @Value("${app.storage.shared-root}")
    private String sharedRoot;
    private final CleanedCsvDetailsLogService cleanedCsvDetailsLogService;

    private final ResponseBuilder responseBuilder;

    public CommonResponse getCleanedFileDetails(String relativePath) {

        try {
            CleanedCsvDetailsJsonDto detailsObject = cleanedCsvDetailsLogService.findByRelativePath(relativePath);
            if (detailsObject != null) {
                return responseBuilder.buildSuccessResponse("Success: Directly get from DB", detailsObject);
            }
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
            String fileBaseName = relativePath.replace(".csv", "");
            String outputJsonPath = Path.of(sharedRoot, "reports", fileBaseName + "_details.json").toString();
            //"F:\GT-v1-shared-storage\cleaned\USDJPY_M5_cleaned.csv"
//            String cleanedRootPath = Path.of(cleanedCsvDir, relativePath).toString();
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "-m", "gt_v1_engine.cleaned_csv_details",
                    "--root-path", cleanedCsvDir,
                    "--relative-path", relativePath,
                    "--output-json", outputJsonPath
            );

            pb.directory(new File("F:\\GT-v1-engine"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String summary = readFileIfExists(outputJsonPath);
                log.error(
                        "Error while generating json raw data for exitCode={}, output={}, summary={}",
                        exitCode,
                        output,
                        summary
                );
                return responseBuilder.buildHandledErrorResponse(
                        "Error while cleaning raw data for path=" + outputJsonPath,
                        Map.of(
                                "exitCode", exitCode,
                                "output", output,
                                "summaryPath", outputJsonPath,
                                "summary", summary
                        )
                );
            }

            String jsonPath = "F:\\GT-v1-shared-storage\\reports\\USDJPY_M5_cleaned_details.json";
            log.info("LOG:: out put path={}, requested json path={}", outputJsonPath, jsonPath);

            CleanedCsvDetailsJsonDto savedDetails = cleanedCsvDetailsLogService.readJsonAndSave(jsonPath);

            return responseBuilder.buildSuccessResponse("Success: Data save to db", savedDetails);
        } catch (IOException e) {
            return responseBuilder.buildSoftErrorResponse(e.getMessage(), relativePath);
        } catch (Exception e) {
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
}
