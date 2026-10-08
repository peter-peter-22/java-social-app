package com.example.uploads_service.api.uploads;

public interface OriginalUploadRepository {
    Upload insert(InsertUpload upload) throws UploadKeyTakenException;

    Upload findById(String uploadKey);

    void deleteById(String uploadKey);
}
