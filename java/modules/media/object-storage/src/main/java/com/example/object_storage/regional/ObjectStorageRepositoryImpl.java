package com.example.object_storage.regional;

import com.example.object_storage.RecoverableObjectStoreException;
import com.example.object_storage.regional.args.*;
import com.example.object_storage.tags.ObjectTag;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RequiredArgsConstructor
@NullMarked
class ObjectStorageRepositoryImpl implements ObjectStorageRepository {
    private final MinioClient minioClient;

    @Override
    public void deleteObject(DeleteLocalObjectArgs args) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(args.getBucket())
                    .object(args.getKey())
                    .build());
        } catch (Exception e) {
            throw failure("Failed to delete object from MinIO", e);
        }
    }

    @Override
    public InputStream getObject(GetLocalObjectArgs args) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .build()
            );
        } catch (Exception e) {
            throw failure("Failed to read object from MinIO", e);
        }
    }

    @Override
    public boolean objectExists(LocalObjectExistsArgs args) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw failure(e);
        } catch (Exception e) {
            throw failure(e);
        }
    }

    @Override// TODO check integration test
    public void putObject(PutLocalObjectArgs args) {
        try {
            var builder = PutObjectArgs.builder()
                    .bucket(args.getBucket())
                    .object(args.getKey())
                    .stream(args.getInputStream(), args.getContentLength(), -1)
                    .contentType(args.getContentType())
                    .tags(args.getTags() == null ? null : ObjectTag.toMap(args.getTags()));
            if (!args.isAllowOverwrite())
                builder.extraHeaders(Map.of("If-None-Match", "*"));
            minioClient.putObject(builder.build());
        } catch (Exception e) {
            throw failure("Failed to upload input stream", e);
        }
    }

    @Override
    public void downloadObject(DownloadLocalObjectArgs args) {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .filename(args.getDestinationPath())
                            .build()
            );
        } catch (Exception e) {
            throw failure("Failed to download file", e);
        }
    }

    @Override// TODO check integration test
    public void uploadObject(UploadLocalObjectArgs args) {
        try {
            var builder = UploadObjectArgs.builder()
                    .bucket(args.getBucket())
                    .object(args.getKey())
                    .filename(args.getSourcePath())
                    .contentType(args.getContentType())
                    .tags(args.getTags() == null ? null : ObjectTag.toMap(args.getTags()));
            if (!args.isAllowOverwrite())
                builder.extraHeaders(Map.of("If-None-Match", "*"));
            minioClient.uploadObject(builder.build());
        } catch (Exception e) {
            throw failure("Failed to upload file", e);
        }
    }

    @Override// TODO check integration test
    public void setTags(SetTagsForLocalObjectArgs args) {
        // TODO test if this function keep the previous tags or not
        // giving null as tags causes an error instead of removing all tags, so the tags are probably imperative
        // this means that the tag list must not be nullable anymore and the way the tags are stored need to be reworked
        try {
            minioClient.setObjectTags(
                    SetObjectTagsArgs.builder()
                            .bucket(args.getBucket())
                            .object(args.getKey())
                            .tags(args.getTags() == null ? null : ObjectTag.toMap(args.getTags()))
                            .build()
            );
        } catch (Exception e) {
            throw failure("Failed to set tags for object", e);
        }
    }

    /**
     * Classifies the Minio failure: transient transport and server failures become
     * {@link RecoverableObjectStoreException} so a worker can retry them; permanent failures stay a
     * plain {@link RuntimeException} with the original cause.
     */
    private static RuntimeException failure(String message, Exception cause) {
        return isTransient(cause)
                ? new RecoverableObjectStoreException(message, cause)
                : new RuntimeException(message, cause);
    }

    private static RuntimeException failure(Exception cause) {
        return isTransient(cause)
                ? new RecoverableObjectStoreException(cause.toString(), cause)
                : new RuntimeException(cause);
    }

    /** Whether the failure is transient enough to retry. */
    private static boolean isTransient(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof IOException
                    || cause instanceof InsufficientDataException
                    || cause instanceof InvalidResponseException
                    || cause instanceof InternalException
                    || cause instanceof ServerException) {
                return true;
            }
            if (cause instanceof ErrorResponseException errorResponse) {
                var response = errorResponse.response();
                if (response != null && response.code() >= 500)
                    return true;
            }
        }
        return false;
    }
}
