package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public interface ObjectStorageRepository {
    @NonNull String getDownloadUrl(@NonNull ObjectLocation location);

    @NonNull String getSignedUploadFormUrl(@NonNull ObjectLocation location);

    @NonNull String getPreSignedDownloadUrl(@NonNull GetPreSignedDownloadUrlArgs args);

    @NonNull Map<String, String> getPreSignedUploadForm(@NonNull GetPreSignedUploadFormArgs args);

    void deleteObject(@NonNull ObjectLocation location);

    @NonNull InputStream getObject(@NonNull ObjectLocation location);

    boolean objectExists(@NonNull ObjectLocation location);

    void putObject(@NonNull ObjectLocation location, @NonNull InputStream inputStream, long contentLength, @NonNull String contentType);

    // TODO: add unit tests
    void downloadObject(@NonNull ObjectLocation location, @NonNull Path filePath);

    void uploadObject(@NonNull ObjectLocation location, @NonNull Path filePath, @NonNull String contentType);
}