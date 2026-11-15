package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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
                .extension(insert.getExtension())
                .contentType(insert.getContentType())
                .bytes(insert.getBytes())
                .deliveryProtocol(insert.getDeliveryProtocol())
                .status(StoredAssetStatus.UPLOADING)
                .objects(List.copyOf(insert.getObjects()))
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

    @Override
    @Transactional
    public void setStatusAll(Collection<UUID> ids, StoredAssetStatus status) {
        if (storedAssetCrudRepository.updateStatusAll(ids, status) != ids.size()) {
            throw new IllegalArgumentException("Could not update the status of every stored asset");
        }
    }
}
