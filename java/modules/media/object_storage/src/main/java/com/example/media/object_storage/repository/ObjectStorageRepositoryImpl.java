package com.example.media.object_storage.repository;

import com.example.media.object_storage.MinioProperties;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class ObjectStorageRepositoryImpl implements ObjectStorageRepository {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public @NotNull String getDownloadUrl(@NotNull String bucket, @NotNull String objectPath) {
        return String.format("%s/%s/%s", minioProperties.endpoint(), bucket, objectPath);
    }

    @Override
    public @NotNull String getBucketUrl(@NotNull String bucket) {
        return String.format("%s/%s", minioProperties.endpoint(), bucket);
    }

    @Override
    public @NotNull String getPreSignedDownloadUrl(@NotNull GetPreSignedDownloadUrlArgs args) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(args.getBucket())
                            .object(args.getObjectPath())
                            .expiry(args.getExpiresIn(), TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed download URL", e);
        }
    }

    @Override
    public @NotNull Map<String, String> getPreSignedUploadForm(@NotNull GetPreSignedUploadFormArgs args) {
        PostPolicy postPolicy = new PostPolicy(
                args.getBucket(),
                ZonedDateTime.now().plus(args.getExpiration(),args.getTimeUnit())
        );

        if (args.getObjectPath() != null)
            postPolicy.addEqualsCondition("key", args.getObjectPath());
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
    public void deleteObject(@NotNull String bucket, @NotNull String objectPath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object from MinIO", e);
        }
    }

    @Override
    public @NotNull InputStream getObject(@NotNull String bucket, @NotNull String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object from MinIO", e);
        }
    }

    @Override
    public void uploadObject(@NotNull String filePath, @NotNull String objectPath, @NotNull String bucketName, @NotNull String contentType) {
        try {
            var args = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .filename(filePath)
                    .contentType(contentType)
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean objectExists(@NotNull String bucket, @NotNull String objectPath) {
        try {
            var args = StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
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
    public void putObject(@NotNull String bucket, @NotNull String objectPath, @NotNull InputStream inputStream, long contentLength, @NotNull String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .stream(inputStream, contentLength, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload input stream", e);
        }
    }
}
