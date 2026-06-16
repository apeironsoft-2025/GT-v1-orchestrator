package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service;

import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.entity.CleanedCsvDetailsLog;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.model.CleanedCsvDetailsJsonDto;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.repository.CleanedCsvDetailsLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanedCsvDetailsLogService {
    private final ObjectMapper objectMapper;
    private final CleanedCsvDetailsLogRepository repository;

    public CleanedCsvDetailsJsonDto readJsonAndSave(String jsonFilePath) throws Exception {
        CleanedCsvDetailsJsonDto dto = objectMapper.readValue(
                new File(jsonFilePath),
                CleanedCsvDetailsJsonDto.class
        );

        CleanedCsvDetailsLog entity = CleanedCsvDetailsMapper.toEntity(dto);
        CleanedCsvDetailsLog e = repository.save(entity);
        return CleanedCsvDetailsMapper.toDto(e);
    }

    //  "relative_path": "USDJPY_M5_cleaned.csv"
    public CleanedCsvDetailsJsonDto findByRelativePath(String fileName) {
        try {
            Optional<CleanedCsvDetailsLog> entity = repository.findById(fileName);
            return entity.map(CleanedCsvDetailsMapper::toDto).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
