package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.repository;

import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.entity.CleanedCsvDetailsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleanedCsvDetailsLogRepository extends JpaRepository<CleanedCsvDetailsLog, String> {
}
