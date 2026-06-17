package com.apeironsoft.GT_v1_orchestrator.indicator.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gt-v1.python")
public class PythonExperimentProperties {

    /**
     * Example:
     * python
     * or
     * F:/GT-v1-engine/.venv/Scripts/python.exe
     */
    private String executable = "python";

    /**
     * Example:
     * F:/GT-v1-engine
     */
    private String engineRootPath;

    private String cleanedRootPath;

    /**
     * Example:
     * scripts/run_macd_td_ts_from_cleaned_csv.py
     */
    private String macdScriptRelativePath;

    /**
     * Example:
     * F:/GT-v1-shared-storage/indicators
     */
    private String outputDir;

    private String emaStackScriptRelativePath;

    private String emaStackOutputDir;

    private String indicatorsRootPath;

    private String experimentsRootPath;

    private String mergeTdTsScriptRelativePath;

    private long timeoutSeconds = 300;
}
