package com.apeironsoft.GT_v1_orchestrator.raw_file_handler.model;

import java.time.Instant;
import java.util.List;

public record LocalFileListResponse(
        int fileCount,
        String rootPath,
        List<LocalFileInfo> files
) {
    public record LocalFileInfo(
            String fileName,
            String relativePath,
            String absolutePath,
            long sizeBytes,
            Instant lastModifiedAt
    ) {
    }
}
