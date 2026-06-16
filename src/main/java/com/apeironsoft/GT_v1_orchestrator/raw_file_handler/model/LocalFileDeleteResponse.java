package com.apeironsoft.GT_v1_orchestrator.raw_file_handler.model;

import java.time.Instant;

public record LocalFileDeleteResponse(
        boolean success,
        String message,
        String deletedFileName,
        String deletedRelativePath,
        boolean fileDeleted,
        boolean parentDirectoryDeleted,
        Instant deletedAt
) {
}
