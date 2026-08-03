package com.example.uploads_api.v2.uploads.upload_registry;

public interface OriginalUploadRepository {
    Upload insert(InsertUpload upload);

    Upload findById(String uploadKey);

    void deleteById(String uploadKey);
}
