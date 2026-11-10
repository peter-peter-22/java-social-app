package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@NullMarked
public class StoredAssetRepositoryImpl implements StoredAssetRepository {
    private final StoredAssetCrudRepository storedAssetCrudRepository;
    private final ImageMetadataCrudRepository imageMetadataCrudRepository;
    private final VideoMetadataCrudRepository videoMetadataCrudRepository;

    @Override
    @Transactional
    public StoredAsset insert(InsertStoredAsset insert) {
        var inserted = storedAssetCrudRepository.saveAndFlush(StoredAssetEntity.builder()
                .homeRegion(insert.getHomeRegion())
                .objectBucket(insert.getObjectBucket())
                .directory(insert.isDirectory())
                .entryPoint(insert.getEntryPoint())
                .mimeType(insert.getMimeType())
                .extension(insert.getExtension())
                .bytes(insert.getBytes())
                .deliveryProtocol(insert.getDeliveryProtocol())
                .build());
        return StoredAssetEntity.toModel(inserted);
    }

    @Override
    @Transactional(readOnly = true)
    public @Nullable StoredAsset findById(UUID id) {
        return storedAssetCrudRepository.findByIdWithMetadata(id)
                .map(StoredAssetEntity::toModel)
                .orElse(null);
    }

    @Override
    public void delete(UUID id) {
        storedAssetCrudRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void setImageMetadata(UUID storedAssetId, ImageMetadata metadata) {
        imageMetadataCrudRepository.saveAndFlush(ImageMetadataEntity.builder()
                .storedAsset(storedAssetCrudRepository.getReferenceById(storedAssetId))
                .resolutionX(metadata.getResolutionX())
                .resolutionY(metadata.getResolutionY())
                .build());
    }

    @Override
    @Transactional
    public void setVideoMetadata(UUID storedAssetId, VideoMetadata metadata) {
        videoMetadataCrudRepository.saveAndFlush(VideoMetadataEntity.builder()
                .storedAsset(storedAssetCrudRepository.getReferenceById(storedAssetId))
                .resolutionX(metadata.getResolutionX())
                .resolutionY(metadata.getResolutionY())
                .durationMilliseconds(metadata.getDurationMilliseconds())
                .build());
    }
}
