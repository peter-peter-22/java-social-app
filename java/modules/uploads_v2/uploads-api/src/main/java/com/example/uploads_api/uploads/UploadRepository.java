package com.example.uploads_api.uploads;

public interface UploadRepository {
    Upload insert(InsertUpload upload) throws UploadKeyTakenException;

    Upload findById(String uploadKey);

    void deleteById(String uploadKey);
}
