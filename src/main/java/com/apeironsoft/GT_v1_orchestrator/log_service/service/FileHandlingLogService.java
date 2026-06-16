package com.apeironsoft.GT_v1_orchestrator.log_service.service;

import com.apeironsoft.GT_v1_orchestrator.log_service.entity.FileHandlingLog;
import com.apeironsoft.GT_v1_orchestrator.log_service.repository.FileHandlingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileHandlingLogService {
    private final FileHandlingLogRepository fileHandlingLogRepository;

    public FileHandlingLog save(
            String jobId,
            String originalFilename,
            Long fileSize,
            String uploadStatus,
            String localStatus,
            String relativePath
    ) {
        FileHandlingLog log = FileHandlingLog.builder()
                .jobId(jobId)
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .uploadStatus(uploadStatus)
                .localStatus(localStatus)
                .relativePath(relativePath)
                .build();
        try {
            return fileHandlingLogRepository.save(log);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public FileHandlingLog save(FileHandlingLog fileHandlingLog) {
        try {
            return fileHandlingLogRepository.save(fileHandlingLog);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
