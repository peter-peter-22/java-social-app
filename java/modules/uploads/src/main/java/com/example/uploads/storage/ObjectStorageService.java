package com.example.uploads.storage;

import com.example.uploads.registry.UploadId;

public interface ObjectStorageService {
    UploadId createUploadSession(String bucketName);
    void finishUploadSession(UploadId id);
    String getDownloadUrl(UploadId id);
}
