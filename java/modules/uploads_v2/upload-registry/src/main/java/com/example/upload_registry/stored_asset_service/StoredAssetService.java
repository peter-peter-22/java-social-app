package com.example.upload_registry.stored_asset_service;

import com.example.object_storage.global.GlobalObjectRepository;
import com.example.object_storage.regional.MinioProperties;
import com.example.object_storage.regional.args.*;
import com.example.object_storage.tags.ObjectTag;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@NullMarked
@EnableConfigurationProperties(StoredAssetConfiguration.class)
public class StoredAssetService {
    private final GlobalObjectRepository globalObjectRepository;
    private final StoredAssetRepository storedAssetRepository;
    private final StoredAssetConfiguration storedAssetConfiguration;
    private final MinioProperties minioProperties;

    private final String bucket = minioProperties.uploadsConfiguration().bucketName();
    private final List<ObjectTag> expirationTags = List.of(ObjectTag.EXPIRES);

    public void setTemporaryFile(StoredAsset storedAsset, InputStream inputStream) {
        globalObjectRepository.putObject(
                storedAsset.getHomeRegion(),
                PutLocalObjectArgs.builder()
                        .inputStream(inputStream)
                        .contentType(storedAsset.getContentType())
                        .contentLength(storedAsset.getBytes())
                        .key(storedAsset.getMainObjectKey())
                        .tags(expirationTags)
                        .bucket(bucket)
                        .build()
        );
    }

    public void setTemporaryFile(StoredAsset storedAsset, Path path) {
        globalObjectRepository.uploadFile(
                storedAsset.getHomeRegion(),
                UploadLocalObjectArgs.builder()
                        .sourcePath(path.toString())
                        .contentType(storedAsset.getContentType())
                        .key(storedAsset.getMainObjectKey())
                        .tags(expirationTags)
                        .bucket(bucket)
                        .build()
        );
    }

    public void setTemporaryFiles(StoredAsset storedAsset, Collection<Path> paths) {
        if (paths.isEmpty())
            throw new IllegalStateException("The directory that is uploaded for a stored asset is empty.");
        if (!storedAsset.isDirectory() && paths.size() > 1)
            throw new IllegalStateException("""
                    Attempted to upload multiple files to a stored asset that is not marked as a directory.
                    The uploaded directory must contain exactly one file.
                    The encoding if a transformation is registered as single output instead of directory by mistake?""");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = paths.stream()
                    .map(path -> CompletableFuture.runAsync(
                            () -> setTemporaryFile(storedAsset, path),
                            executor
                    ))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
        }
    }

    public InputStream getMainFileStream(StoredAsset storedAsset) {
        return globalObjectRepository.getObject(
                storedAsset.getHomeRegion(),
                GetLocalObjectArgs.builder()
                        .key(storedAsset.getMainObjectKey())
                        .bucket(bucket)
                        .build()
        );
    }

    public InputStream getDirectoryFileStream(StoredAsset storedAsset, String relativePath) {
        return globalObjectRepository.getObject(
                storedAsset.getHomeRegion(),
                GetLocalObjectArgs.builder()
                        .key(storedAsset.getDirectoryPrefix() + "/" + relativePath)
                        .bucket(bucket)
                        .build()
        );
    }

    public void downloadMainFile(StoredAsset storedAsset, Path path) {
        globalObjectRepository.downloadFile(
                storedAsset.getHomeRegion(),
                DownloadLocalObjectArgs.builder()
                        .destinationPath(path.toString())
                        .key(storedAsset.getMainObjectKey())
                        .bucket(bucket)
                        .build()
        );
    }

    public StoredAsset createStoredAsset(CreateStoredAsset insert) {
        return storedAssetRepository.insert(
                InsertStoredAsset.builder()
                        .homeRegion(globalObjectRepository.getHomeRegion())
                        .objectBucket(bucket)
                        .directory(insert.isDirectory())
                        .entryPoint(insert.getEntryPoint())
                        .mimeType(insert.getMimeType())
                        .extension(insert.getExtension())
                        .bytes(insert.getBytes())
                        .deliveryProtocol(insert.getDeliveryProtocol())
                        .build()
        );
    }

    /**
     * Set object storage tags for all object keys in the provided assets. The assets must be from the same region.
     */
    public void tagAllAssets(Collection<StoredAsset> assets, @Nullable Collection<ObjectTag> tags) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = new ArrayList<Future<?>>();
            for (StoredAsset asset : assets) {
                for (var key : asset.getAllObjectKeys()) {
                    futures.add(
                            executor.submit(
                                    () -> globalObjectRepository.setTags(
                                            asset.getHomeRegion(),
                                            SetTagsForLocalObjectArgs.builder()
                                                    .bucket(bucket)
                                                    .key(key)
                                                    .tags(tags)
                                                    .build()
                                    )
                            )
                    );
                }
            }
            for (var future : futures) {
                future.get();
            }
        } catch (Exception e) {
            executor.shutdownNow();
            throw new RuntimeException("Error while setting the tags of the assets:", e);
        }
    }

    public void setAssetStatus(Collection<UUID> assetIds, StoredAssetStatus status) {
        storedAssetRepository.setStatusAll(assetIds, status);
    }

    /**
     * Returns true if all registered objects of all assets exist, returns false otherwise.
     */
    public boolean objectsExist(Collection<StoredAsset> assets) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (executor) {
            var futures = new ArrayList<Future<Boolean>>();
            for (StoredAsset asset : assets) {
                for (var key : asset.getAllObjectKeys()) {
                    futures.add(
                            executor.submit(() -> globalObjectRepository.objectExists(
                                    asset.getHomeRegion(),
                                    LocalObjectExistsArgs.builder().key(key).bucket(bucket).build()
                            ))
                    );
                }
            }
            for (var future : futures) {
                if (!future.get())
                    return false;
            }
            return true;
        } catch (Exception e) {
            executor.shutdownNow();
            throw new RuntimeException("Error while setting the tags of the assets:", e);
        }
    }
}
