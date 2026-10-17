package com.example.uploads_api.uploads;


import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface UploadRepository {
    UUID insert(InsertUpload upload) throws UploadKeyTakenException;

    void updateStatus(UUID uploadId, UploadStatus status);

    Upload findByKey(String uploadKey);
}
