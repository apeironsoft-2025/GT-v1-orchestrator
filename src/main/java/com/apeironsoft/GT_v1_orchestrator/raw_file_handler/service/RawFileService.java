package com.apeironsoft.GT_v1_orchestrator.raw_file_handler.service;

import com.apeironsoft.GT_v1_orchestrator.common.ResponseBuilder;
import com.apeironsoft.GT_v1_orchestrator.common.CommonResponse;
import com.apeironsoft.GT_v1_orchestrator.raw_file_handler.model.LocalFileDeleteResponse;
import com.apeironsoft.GT_v1_orchestrator.raw_file_handler.model.LocalFileListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawFileService {

    @Value("${app.storage.raw-csv-dir}")
    private String rawCsvDir;

    private final ResponseBuilder responseBuilder;

    private String saveFileIntoLocal(MultipartFile file, String fileName) {
        log.info("Saving file into local storage");
        try {
            Path jobDir = Paths.get(rawCsvDir);
            Path targetPath = jobDir.resolve(fileName + ".csv");

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully saved file into local storage");
            return targetPath.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    public CommonResponse saveToLocalAndCreateLog(MultipartFile file, String fileName) {

        String localData = saveFileIntoLocal(file, fileName);

        return responseBuilder.buildSuccessResponse(
                "File save in local and upload to cloud is success",
                Map.of(
                        "fileName", fileName,
                        "status", "success",
                        "local_data", localData
                )
        );

    }

    public CommonResponse getSavedLocalFiles() throws IOException {
        Path rootPath = Paths.get(rawCsvDir).toAbsolutePath().normalize();

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
        }
    }

    public CommonResponse deleteRawLocalFile(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            log.error("LOG:: relative path is null or blank");
            return responseBuilder.buildHandledErrorResponse("relative path is required");
        }

        Path rootPath = Paths.get(rawCsvDir).toAbsolutePath().normalize();

        Path targetPath = rootPath.resolve(relativePath).normalize();
        log.info("LOG:: targetPath={}, rootPath={}", targetPath, rootPath);

        if (!targetPath.startsWith(rootPath)) {
            return responseBuilder.buildSoftErrorResponse(
                    "failed",
                    new LocalFileDeleteResponse(
                            false,
                            "Invalid file path. Delete outside storage root is not allowed.",
                            null,
                            relativePath,
                            false,
                            false,
                            Instant.now()
                    )
            );
        }

        if (!Files.exists(targetPath)) {
            return responseBuilder.buildSoftErrorResponse(
                    "failed",
                    new LocalFileDeleteResponse(
                            false,
                            "File does not exist",
                            null,
                            relativePath,
                            false,
                            false,
                            Instant.now()
                    )
            );

        }

        if (!Files.isRegularFile(targetPath)) {
            return responseBuilder.buildSoftErrorResponse(
                    "failed",
                    new LocalFileDeleteResponse(
                            false,
                            "Selected path is not a file",
                            null,
                            relativePath,
                            false,
                            false,
                            Instant.now()
                    )
            );
        }

        String deletedFileName = targetPath.getFileName().toString();

        boolean fileDeleted = Files.deleteIfExists(targetPath);
        log.info("LOG:: deleted file from local");

        boolean parentDirectoryDeleted = false;

        Path parentDir = targetPath.getParent();

        if (parentDir != null && !parentDir.equals(rootPath)) {
            try {
                Files.delete(parentDir);
                parentDirectoryDeleted = true;
            } catch (DirectoryNotEmptyException ignored) {
            }
        }

        return responseBuilder.buildSuccessResponse("success",
                new LocalFileDeleteResponse(
                        true,
                        "Local raw file deleted successfully",
                        deletedFileName,
                        rootPath.relativize(targetPath).toString(),
                        fileDeleted,
                        parentDirectoryDeleted,
                        Instant.now()
                )
        );
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
