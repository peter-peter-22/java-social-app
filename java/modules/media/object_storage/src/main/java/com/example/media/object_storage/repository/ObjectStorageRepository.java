package com.example.media.object_storage.repository;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

public interface ObjectStorageRepository {
    @NotNull String getDownloadUrl(@NotNull String bucket, @NotNull String objectPath);

    @NotNull String getBucketUrl(@NotNull String bucket);

    @NotNull String getPreSignedDownloadUrl(@NotNull GetPreSignedDownloadUrlArgs args);

    @NotNull Map<String, String> getPreSignedUploadForm(@NotNull GetPreSignedUploadFormArgs args);

    void deleteObject(@NotNull String bucket, @NotNull String objectPath);

    @NotNull InputStream getObject(@NotNull String bucket, @NotNull String objectPath);

    void uploadObject(@NotNull String filePath, @NotNull String objectPath, @NotNull String bucketName, @NotNull String contentType);

    boolean objectExists(@NotNull String bucket, @NotNull String objectPath);

    void putObject(@NotNull String bucket, @NotNull String objectPath, @NotNull InputStream inputStream, long contentLength, @NotNull String contentType);
}
