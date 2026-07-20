package com.example.object_storage.repository;

import com.example.object_storage.MinioProperties;
import com.example.uploads_api.uploads.ObjectLocation;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
class ObjectStorageRepositoryImpl implements ObjectStorageRepository {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public @NonNull String getDownloadUrl(@NonNull ObjectLocation location) {
        return String.format("%s/%s/%s", minioProperties.endpoint(), location.bucket(), location.key());
    }

    private @NonNull String getBucketUrl(@NonNull String bucket) {
        return String.format("%s/%s", minioProperties.endpoint(), bucket);
    }

    @Override
    public @NonNull String getSignedUploadFormUrl(@NonNull ObjectLocation location) {
        return getBucketUrl(location.bucket());
    }

    @Override
    public @NonNull String getPreSignedDownloadUrl(@NonNull GetPreSignedDownloadUrlArgs args) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(args.getLocation().bucket())
                            .object(args.getLocation().key())
                            .expiry(args.getExpiresIn(), TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed download URL", e);
        }
    }

    @Override
    public @NonNull Map<String, String> getPreSignedUploadForm(@NonNull GetPreSignedUploadFormArgs args) {
        PostPolicy postPolicy = new PostPolicy(
                args.getLocation().bucket(),
                ZonedDateTime.now().plus(args.getExpiration(),args.getTimeUnit())
        );

        postPolicy.addEqualsCondition("key", args.getLocation().key());
        if (args.getContentType() != null)
            postPolicy.addEqualsCondition("Content-Type", args.getContentType());
        if (args.getContentLengthRange() != null)
            postPolicy.addContentLengthRangeCondition(args.getContentLengthRange().minBytes(), args.getContentLengthRange().maxBytes());

        try {
            return minioClient.getPresignedPostFormData(postPolicy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed upload URL", e);
        }
    }

    @Override
    public void deleteObject(@NonNull ObjectLocation location) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(location.bucket())
                    .object(location.key())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object from MinIO", e);
        }
    }

    @Override
    public @NonNull InputStream getObject(@NonNull ObjectLocation location) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.key())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object from MinIO", e);
        }
    }

    @Override
    public void uploadObject(@NonNull String filePath, @NonNull ObjectLocation location, @NonNull String contentType) {
        try {
            var args = UploadObjectArgs.builder()
                    .bucket(location.bucket())
                    .object(location.key())
                    .filename(filePath)
                    .contentType(contentType)
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean objectExists(@NonNull ObjectLocation location) {
        try {
            var args = StatObjectArgs.builder()
                    .bucket(location.bucket())
                    .object(location.key())
                    .build();
            var stat = minioClient.statObject(args);
            System.out.println(stat);
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putObject(@NonNull ObjectLocation location, @NonNull InputStream inputStream, long contentLength, @NonNull String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.key())
                            .stream(inputStream, contentLength, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload input stream", e);
        }
    }
}
