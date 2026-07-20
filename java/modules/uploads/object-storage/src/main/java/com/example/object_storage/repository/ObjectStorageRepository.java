package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.util.Map;

public interface ObjectStorageRepository {
    @NotNull String getDownloadUrl(@NonNull ObjectLocation location);

    @NonNull String getSignedUploadFormUrl(@NotNull ObjectLocation location);

    @NotNull String getPreSignedDownloadUrl(@NotNull GetPreSignedDownloadUrlArgs args);

    @NotNull Map<String, String> getPreSignedUploadForm(@NotNull GetPreSignedUploadFormArgs args);

    void deleteObject(@NonNull ObjectLocation location);

    @NotNull InputStream getObject(@NonNull ObjectLocation location);

    void uploadObject(@NotNull String filePath, @NonNull ObjectLocation location, @NotNull String contentType);

    boolean objectExists(@NonNull ObjectLocation location);

    void putObject(@NonNull ObjectLocation location, @NotNull InputStream inputStream, long contentLength, @NotNull String contentType);
}