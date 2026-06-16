package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.service;

import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.entity.CleanedCsvDetailsLog;
import com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.model.CleanedCsvDetailsJsonDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class CleanedCsvDetailsMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CleanedCsvDetailsLog toEntity(CleanedCsvDetailsJsonDto dto) {
        try {
            return CleanedCsvDetailsLog.builder()
                    .fileName(dto.getFileName())
                    .status(dto.getStatus())
                    .errorMessage(dto.getErrorMessage())
                    .rootPath(dto.getRootPath())
                    .relativePath(dto.getRelativePath())
                    .absolutePath(dto.getAbsolutePath())
                    .rowCount(dto.getRowCount())
                    .startDateTime(dto.getStartDateTime())
                    .endDateTime(dto.getEndDateTime())
                    .maxPriceFromOhlc(dto.getMaxPriceFromOhlc())
                    .minPriceFromOhlc(dto.getMinPriceFromOhlc())
                    .averagePriceFromOhlc(dto.getAveragePriceFromOhlc())
                    .medianPriceFromOhlc(dto.getMedianPriceFromOhlc())
                    .first2RowsJson(objectMapper.writeValueAsString(dto.getFirst2Rows()))
                    .last2RowsJson(objectMapper.writeValueAsString(dto.getLast2Rows()))
                    .mid2RowsJson(objectMapper.writeValueAsString(dto.getMid2Rows()))
                    .requiredColumnsJson(objectMapper.writeValueAsString(dto.getRequiredColumns()))
                    .availableColumnsJson(objectMapper.writeValueAsString(dto.getAvailableColumns()))
                    .analyzerStartedAt(dto.getStartedAt())
                    .analyzerFinishedAt(dto.getFinishedAt())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map cleaned CSV details DTO to entity", e);
        }
    }


    public static CleanedCsvDetailsJsonDto toDto(CleanedCsvDetailsLog entity) {
        CleanedCsvDetailsJsonDto dto = new CleanedCsvDetailsJsonDto();

        dto.setStatus(entity.getStatus());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setRootPath(entity.getRootPath());
        dto.setRelativePath(entity.getRelativePath());
        dto.setAbsolutePath(entity.getAbsolutePath());
        dto.setFileName(entity.getFileName());
        dto.setRowCount(entity.getRowCount());
        dto.setStartDateTime(entity.getStartDateTime());
        dto.setEndDateTime(entity.getEndDateTime());
        dto.setMaxPriceFromOhlc(entity.getMaxPriceFromOhlc());
        dto.setMinPriceFromOhlc(entity.getMinPriceFromOhlc());
        dto.setAveragePriceFromOhlc(entity.getAveragePriceFromOhlc());
        dto.setMedianPriceFromOhlc(entity.getMedianPriceFromOhlc());
        dto.setStartedAt(entity.getAnalyzerStartedAt());
        dto.setFinishedAt(entity.getAnalyzerFinishedAt());

        try {
            dto.setFirst2Rows(objectMapper.readValue(entity.getFirst2RowsJson(), List.class));
            dto.setLast2Rows(objectMapper.readValue(entity.getLast2RowsJson(), List.class));
            dto.setMid2Rows(objectMapper.readValue(entity.getMid2RowsJson(), List.class));
            dto.setRequiredColumns(objectMapper.readValue(entity.getRequiredColumnsJson(), List.class));
            dto.setAvailableColumns(objectMapper.readValue(entity.getAvailableColumnsJson(), List.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to map cleaned CSV details entity to DTO", e);
        }

        return dto;
    }
}
