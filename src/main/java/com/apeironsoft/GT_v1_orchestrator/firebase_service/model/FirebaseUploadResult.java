package com.apeironsoft.GT_v1_orchestrator.firebase_service.model;

public record FirebaseUploadResult(
        String bucketName,
        String objectPath,
        String gsUri,
        String originalFilename,
        long fileSizeBytes
) {
}
