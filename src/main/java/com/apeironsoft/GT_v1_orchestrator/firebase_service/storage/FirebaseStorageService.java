package com.apeironsoft.GT_v1_orchestrator.firebase_service.storage;

import com.apeironsoft.GT_v1_orchestrator.firebase_service.model.FirebaseUploadResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FirebaseStorageService {

    private final Bucket bucket;

    public FirebaseStorageService(Bucket bucket) {
        this.bucket = bucket;
    }

    public FirebaseUploadResult uploadRawCsv(String jobId, MultipartFile file) {
        try {
            String objectPath = "raw-csv/" + jobId + "/input.csv";

            Map<String, String> metadata = new HashMap<>();
            metadata.put("jobId", jobId);
            metadata.put("originalFilename", file.getOriginalFilename());
            metadata.put("uploadedAt", Instant.now().toString());

            Blob blob = bucket.create(
                    objectPath,
                    file.getInputStream(),
                    file.getContentType() != null ? file.getContentType() : "text/csv"
            );

            blob.toBuilder()
                    .setMetadata(metadata)
                    .build()
                    .update();
            log.info("LOG:: uploadCsv success, objectPath={}", objectPath);
            return new FirebaseUploadResult(
                    bucket.getName(),
                    objectPath,
                    "gs://" + bucket.getName() + "/" + objectPath,
                    file.getOriginalFilename(),
                    file.getSize()
            );
        } catch (IOException e) {
            log.error("LOG:: File upload to firebase storage is failed, {}",e.getMessage());
            return null;
        }
    }
}
