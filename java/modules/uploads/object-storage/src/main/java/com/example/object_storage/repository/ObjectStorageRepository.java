package com.example.object_storage.repository;

import com.example.uploads_api.uploads.ObjectLocation;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.util.Map;

public interface ObjectStorageRepository {
    @NonNull String getDownloadUrl(@NonNull ObjectLocation location);

    @NonNull String getSignedUploadFormUrl(@NonNull ObjectLocation location);

    @NonNull String getPreSignedDownloadUrl(@NonNull GetPreSignedDownloadUrlArgs args);

    @NonNull Map<String, String> getPreSignedUploadForm(@NonNull GetPreSignedUploadFormArgs args);

    void deleteObject(@NonNull ObjectLocation location);

    @NonNull InputStream getObject(@NonNull ObjectLocation location);

    void uploadObject(@NonNull String filePath, @NonNull ObjectLocation location, @NonNull String contentType);

    boolean objectExists(@NonNull ObjectLocation location);

    void putObject(@NonNull ObjectLocation location, @NonNull InputStream inputStream, long contentLength, @NonNull String contentType);
}