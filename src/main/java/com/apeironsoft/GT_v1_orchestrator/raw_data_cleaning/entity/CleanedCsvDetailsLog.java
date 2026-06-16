package com.apeironsoft.GT_v1_orchestrator.raw_data_cleaning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class CleanedCsvDetailsLog {

    @Id
    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName; // USDJPY_M5_cleaned.csv

    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "root_path", columnDefinition = "TEXT")
    private String rootPath;

    @Column(name = "relative_path", nullable = false, columnDefinition = "TEXT")
    private String relativePath;

    @Column(name = "absolute_path", columnDefinition = "TEXT")
    private String absolutePath;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "start_datetime")
    private String startDateTime;

    @Column(name = "end_datetime")
    private String endDateTime;

    @Column(name = "max_price_from_ohlc")
    private Double maxPriceFromOhlc;

    @Column(name = "min_price_from_ohlc")
    private Double minPriceFromOhlc;

    @Column(name = "average_price_from_ohlc")
    private Double averagePriceFromOhlc;

    @Column(name = "median_price_from_ohlc")
    private Double medianPriceFromOhlc;

    @Lob
    @Column(name = "first_2_rows", columnDefinition = "LONGTEXT")
    private String first2RowsJson;

    @Lob
    @Column(name = "last_2_rows", columnDefinition = "LONGTEXT")
    private String last2RowsJson;

    @Lob
    @Column(name = "mid_2_rows", columnDefinition = "LONGTEXT")
    private String mid2RowsJson;

    @Lob
    @Column(name = "required_columns", columnDefinition = "LONGTEXT")
    private String requiredColumnsJson;

    @Lob
    @Column(name = "available_columns", columnDefinition = "LONGTEXT")
    private String availableColumnsJson;

    @Column(name = "analyzer_started_at")
    private String analyzerStartedAt;

    @Column(name = "analyzer_finished_at")
    private String analyzerFinishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
