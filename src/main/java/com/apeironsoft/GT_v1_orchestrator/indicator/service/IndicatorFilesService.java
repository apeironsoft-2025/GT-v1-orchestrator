package com.apeironsoft.GT_v1_orchestrator.indicator.service;

import com.apeironsoft.GT_v1_orchestrator.indicator.model.IndicatorFileInfo;
import com.apeironsoft.GT_v1_orchestrator.indicator.model.IndicatorFilesResponse;
import com.apeironsoft.GT_v1_orchestrator.indicator.utils.PythonExperimentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class IndicatorFilesService {

    private static final String DESCRIPTION =
            "Indicator TD/TS CSV output files available as experiment input files.";

    private final PythonExperimentProperties properties;

    public IndicatorFilesResponse getIndicatorFiles() throws IOException {
        Path indicatorsRoot = Path.of(properties.getIndicatorsRootPath()).toAbsolutePath().normalize();

        if (!Files.isDirectory(indicatorsRoot)) {
            throw new IOException("Indicators root path does not exist or is not a directory: " + indicatorsRoot);
        }

        List<IndicatorFileInfo> files;
        try (Stream<Path> pathStream = Files.list(indicatorsRoot)) {
            files = pathStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> toFileInfo(indicatorsRoot, path))
                    .toList();
        }

        List<String> experimentInputFileNames = files.stream()
                .map(IndicatorFileInfo::getExperimentInputFileName)
                .filter(fileName -> fileName.toLowerCase().endsWith(".csv"))
                .toList();

        return IndicatorFilesResponse.builder()
                .description(DESCRIPTION)
                .indicatorsRootPath(indicatorsRoot.toString())
                .experimentInputFileNames(experimentInputFileNames)
                .files(files)
                .build();
    }

    private IndicatorFileInfo toFileInfo(Path indicatorsRoot, Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            return IndicatorFileInfo.builder()
                    .fileName(fileName)
                    .experimentInputFileName(fileName)
                    .relativePath(indicatorsRoot.relativize(filePath).toString())
                    .sizeBytes(Files.size(filePath))
                    .lastModifiedAt(Files.getLastModifiedTime(filePath).toInstant())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read indicator file metadata: " + filePath, e);
        }
    }
}
