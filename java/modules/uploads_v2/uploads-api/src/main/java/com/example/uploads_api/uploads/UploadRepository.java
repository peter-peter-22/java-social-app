package com.example.uploads_api.uploads;


import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface UploadRepository {
    Upload insert(InsertUpload upload) throws UploadKeyTakenException;

    void updateStatus(UUID uploadId, UploadStatus status);

    Upload findByKey(String uploadKey);

    Upload findImageByKey(String uploadKey);

    void deleteById(String uploadKey);
}
