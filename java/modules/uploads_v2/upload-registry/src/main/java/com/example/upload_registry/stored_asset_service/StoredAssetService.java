package com.example.upload_registry.stored_asset_service;

import com.example.object_storage.global.GlobalObjectRepository;
import com.example.object_storage.regional.MinioProperties;
import com.example.object_storage.regional.args.DownloadLocalObjectArgs;
import com.example.object_storage.regional.args.GetLocalObjectArgs;
import com.example.object_storage.regional.args.PutLocalObjectArgs;
import com.example.object_storage.regional.args.UploadLocalObjectArgs;
import com.example.object_storage.tags.ObjectTag;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@NullMarked
@EnableConfigurationProperties(StoredAssetConfiguration.class)
public class StoredAssetService {
    private final GlobalObjectRepository globalObjectRepository;
    private final StoredAssetRepository storedAssetRepository;
    private final StoredAssetConfiguration storedAssetConfiguration;
    private final MinioProperties minioProperties;

    private final String bucket=minioProperties.uploadsConfiguration().bucketName();
    private final List<ObjectTag> expirationTags=List.of(ObjectTag.EXPIRES);

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

    public StoredAsset createStoredAsset(CreateStoredAsset create) {
        return storedAssetRepository.insert(
                InsertStoredAsset.builder()
                        .homeRegion(globalObjectRepository.getHomeRegion())
                        .objectBucket("uploads")
                        .directory(create.isDirectory())
                        .entryPoint(create.getEntryPoint())
                        .mimeType(create.getMimeType())
                        .extension(create.getExtension())
                        .bytes(create.getBytes())
                        .deliveryProtocol(create.getDeliveryProtocol())
                        .expiration(Instant.now().plus(storedAssetConfiguration.getPendingUploadExpiration()))
                        .build()
        );
    }

    public void deleteFailedStoredAssets(){
        // Query for stored assets where the date of expiration is less than current date + upload shutdown duration
        // Delete the files and rows of the found assets
        // Call periodically.
        // How concurrency will work?
        // How failed delete attempts will be handled?
        // TODO implement
    }
}
