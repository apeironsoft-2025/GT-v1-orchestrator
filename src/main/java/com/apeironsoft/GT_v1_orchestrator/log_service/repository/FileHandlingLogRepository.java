package com.apeironsoft.GT_v1_orchestrator.log_service.repository;

import com.apeironsoft.GT_v1_orchestrator.log_service.entity.FileHandlingLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileHandlingLogRepository extends CrudRepository<FileHandlingLog, String> {
}
