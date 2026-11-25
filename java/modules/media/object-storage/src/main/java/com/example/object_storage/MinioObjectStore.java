package com.example.object_storage;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.ObjectStore;
import com.example.object_storage.regional.MinioConfig;
import com.example.object_storage.regional.MinioProperties;
import com.example.object_storage.tags.ObjectTag;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.DownloadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The Minio-backed {@link ObjectStore} of the clean media service.
 * The vendor types, the regions, the tags, and the reconciliation stay inside this adapter:
 * every method translates the plain contract values to Minio calls against the client of the
 * referenced region, and {@link ObjectLifetime} is mapped to the expiration tag the bucket
 * lifecycle rule matches on. Application services see only the port.
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class MinioObjectStore implements ObjectStore {
    private final MinioConfig minioConfig;
    private final MinioProperties minioProperties;

    @Override
    public AssetPlacement homePlacement() {
        return new AssetPlacement(
                minioProperties.homeRegion(),
                minioProperties.uploadsConfiguration().bucketName());
    }

    @Override
    public void put(ObjectRef ref, InputStream data, long bytes, String contentType, ObjectLifetime lifetime) {
        // The port forbids overwrites: the conditional header makes Minio refuse an existing key.
        try {
            client(ref.region()).putObject(PutObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .stream(data, bytes, -1)
                    .contentType(contentType)
                    .tags(tagOf(lifetime).asMap())
                    .extraHeaders(Map.of("If-None-Match", "*"))
                    .build());
        } catch (Exception e) {
            throw failure("Failed to upload input stream", e);
        }
    }

    @Override
    public void put(ObjectRef ref, Path source, String contentType, ObjectLifetime lifetime) {
        try {
            client(ref.region()).uploadObject(UploadObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .filename(source.toString())
                    .contentType(contentType)
                    .tags(tagOf(lifetime).asMap())
                    .extraHeaders(Map.of("If-None-Match", "*"))
                    .build());
        } catch (Exception e) {
            throw failure("Failed to upload file", e);
        }
    }

    @Override
    public InputStream get(ObjectRef ref) {
        try {
            return client(ref.region()).getObject(GetObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .build());
        } catch (Exception e) {
            throw failure("Failed to read object from MinIO", e);
        }
    }

    @Override
    public void download(ObjectRef ref, Path target) {
        try {
            client(ref.region()).downloadObject(DownloadObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .filename(target.toString())
                    .build());
        } catch (Exception e) {
            throw failure("Failed to download file", e);
        }
    }

    /**
     * Tags every object with the lifetime. The tag write is also the existence check: Minio rejects
     * the tagging of a missing object with {@code NoSuchKey}, so the finalize fails on an object
     * that expired before it could be made permanent.
     *
     * <p>Pinned semantics: the Minio tag write replaces the whole tag set of the object (asserted by
     * {@code MinioObjectStoreIT}), so a kept object carries exactly the lifetime tag.</p>
     */
    @Override
    public void setLifetime(List<ObjectRef> refs, ObjectLifetime lifetime) {
        for (var ref : refs) {
            try {
                client(ref.region()).setObjectTags(SetObjectTagsArgs.builder()
                        .bucket(ref.bucket())
                        .object(ref.key())
                        .tags(tagOf(lifetime).asMap())
                        .build());
            } catch (Exception e) {
                throw failure("Failed to set tags for object", e);
            }
        }
    }

    @Override
    public boolean exists(ObjectRef ref) {
        try {
            client(ref.region()).statObject(StatObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .build());
            return true;
        } catch (Exception e) {
            if (e instanceof ErrorResponseException errorResponse
                    && "NoSuchKey".equals(errorResponse.errorResponse().code()))
                return false;
            throw failure(e);
        }
    }

    @Override
    public void delete(ObjectRef ref) {
        try {
            client(ref.region()).removeObject(RemoveObjectArgs.builder()
                    .bucket(ref.bucket())
                    .object(ref.key())
                    .build());
        } catch (Exception e) {
            throw failure("Failed to delete object from MinIO", e);
        }
    }

    private MinioClient client(String region) {
        return minioConfig.getClient(region);
    }

    /** The tag names stay in the adapter; the port carries the lifetime only. */
    private static ObjectTag tagOf(ObjectLifetime lifetime) {
        return switch (lifetime) {
            case TEMPORARY -> ObjectTag.EXPIRES;
            case PERMANENT -> ObjectTag.PERMANENT;
        };
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
