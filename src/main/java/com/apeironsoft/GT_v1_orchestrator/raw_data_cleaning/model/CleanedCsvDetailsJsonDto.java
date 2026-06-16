package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CleanedCsvDetailsJsonDto {

    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("root_path")
    private String rootPath;

    @JsonProperty("relative_path")
    private String relativePath;

    @JsonProperty("absolute_path")
    private String absolutePath;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("row_count")
    private Long rowCount;

    @JsonProperty("start_datetime")
    private String startDateTime;

    @JsonProperty("end_datetime")
    private String endDateTime;

    @JsonProperty("max_price_from_ohlc")
    private Double maxPriceFromOhlc;

    @JsonProperty("min_price_from_ohlc")
    private Double minPriceFromOhlc;

    @JsonProperty("average_price_from_ohlc")
    private Double averagePriceFromOhlc;

    @JsonProperty("median_price_from_ohlc")
    private Double medianPriceFromOhlc;

    @JsonProperty("first_2_rows")
    private List<Map<String, Object>> first2Rows;

    @JsonProperty("last_2_rows")
    private List<Map<String, Object>> last2Rows;

    @JsonProperty("mid_2_rows")
    private List<Map<String, Object>> mid2Rows;

    @JsonProperty("required_columns")
    private List<String> requiredColumns;

    @JsonProperty("available_columns")
    private List<String> availableColumns;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("finished_at")
    private String finishedAt;
}
