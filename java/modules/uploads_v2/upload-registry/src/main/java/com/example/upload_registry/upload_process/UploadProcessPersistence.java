package com.example.upload_registry.upload_process;

import com.example.uploads_api.uploads.upload_process.UploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadProcess;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Basic save and read access used to verify conversion between the upload process domain models and JPA entities.
 */
@NullMarked
public interface UploadProcessPersistence {
    UploadProcess save(UploadProcess process);

    UploadProcess findProcess(String uploadKey);

    UploadAttempt save(UploadAttempt attempt);

    UploadAttempt findAttempt(UUID attemptId);
}
