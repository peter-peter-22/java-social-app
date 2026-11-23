package com.example.object_storage.global;

import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.ObjectStore;
import com.example.object_storage.regional.MinioProperties;
import com.example.object_storage.regional.args.*;
import com.example.object_storage.tags.ObjectTag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * The Minio-backed {@link ObjectStore} of the clean media service.
 * The vendor types, the regions, the tags, and the reconciliation stay inside this adapter:
 * every method translates the plain contract values to the repository arguments, and
 * {@link ObjectLifetime} is mapped to the expiration tag the bucket lifecycle rule matches on.
 * Application services see only the port.
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class MinioObjectStore implements ObjectStore {
    private final GlobalObjectRepository globalObjectRepository;
    private final MinioProperties minioProperties;

    @Override
    public AssetPlacement homePlacement() {
        return new AssetPlacement(
                minioProperties.homeRegion(),
                minioProperties.uploadsConfiguration().bucketName());
    }

    @Override
    public void put(ObjectRef ref, InputStream data, long bytes, String contentType, ObjectLifetime lifetime) {
        // The port forbids overwrites, and the repository only adds them when explicitly allowed.
        globalObjectRepository.putObject(ref.region(), PutLocalObjectArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .inputStream(data)
                .contentLength(bytes)
                .contentType(contentType)
                .tags(List.of(tagOf(lifetime)))
                .build());
    }

    @Override
    public void put(ObjectRef ref, Path source, String contentType, ObjectLifetime lifetime) {
        globalObjectRepository.uploadFile(ref.region(), UploadLocalObjectArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .sourcePath(source.toString())
                .contentType(contentType)
                .tags(List.of(tagOf(lifetime)))
                .build());
    }

    @Override
    public InputStream get(ObjectRef ref) {
        return globalObjectRepository.getObject(ref.region(), GetLocalObjectArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .build());
    }

    @Override
    public void download(ObjectRef ref, Path target) {
        globalObjectRepository.downloadFile(ref.region(), DownloadLocalObjectArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .destinationPath(target.toString())
                .build());
    }

    /**
     * Tags every object with the lifetime. The tag write is also the existence check: Minio rejects
     * the tagging of a missing object with {@code NoSuchKey}, so the finalize fails on an object
     * that expired before it could be made permanent.
     */
    @Override
    public void setLifetime(List<ObjectRef> refs, ObjectLifetime lifetime) {
        for (var ref : refs) {
            globalObjectRepository.setTags(ref.region(), SetTagsForLocalObjectArgs.builder()
                    .bucket(ref.bucket())
                    .key(ref.key())
                    .tags(List.of(tagOf(lifetime)))
                    .build());
        }
    }

    @Override
    public boolean exists(ObjectRef ref) {
        return globalObjectRepository.objectExists(ref.region(), LocalObjectExistsArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .build());
    }

    @Override
    public void delete(ObjectRef ref) {
        globalObjectRepository.delete(ref.region(), DeleteLocalObjectArgs.builder()
                .bucket(ref.bucket())
                .key(ref.key())
                .build());
    }

    /** The tag names stay in the adapter; the port carries the lifetime only. */
    private static ObjectTag tagOf(ObjectLifetime lifetime) {
        return switch (lifetime) {
            case TEMPORARY -> ObjectTag.EXPIRES;
            case PERMANENT -> ObjectTag.PERMANENT;
        };
    }
}
