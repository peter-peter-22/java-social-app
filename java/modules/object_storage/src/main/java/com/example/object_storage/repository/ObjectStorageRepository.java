package com.example.object_storage.repository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ObjectStorageRepository {
    @NotNull String getDownloadUrl(String bucket, String objectPath);

    @NotNull String getPreSignedDownloadUrl();

    @NotNull Map<String, String> getPreSignedUploadUrl(String bucket, String objectPath, Integer expirationSeconds);

    void deleteObject(String path);

    void downloadObject(String path);

    void uploadObject(String filePath, String objectPath, String bucketName, String contentType);
    boolean objectExists(String bucket, String objectPath);
}
