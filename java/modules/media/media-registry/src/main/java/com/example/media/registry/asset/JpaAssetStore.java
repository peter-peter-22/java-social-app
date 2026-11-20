package com.example.media.registry.asset;

import com.example.media.contract.asset.*;
import com.example.media.contract.port.AssetStore;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The asset rows: registrations, metadata, and lifecycle status.
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class JpaAssetStore implements AssetStore {
    private final AssetCrudRepository assetCrudRepository;
    private final ImageMetadataCrudRepository imageMetadataCrudRepository;
    private final VideoMetadataCrudRepository videoMetadataCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Asset registerAsset(NewAsset insert) {
        // The caller reserved the id because it is the object prefix, so it is assigned and not
        // generated here; the objects may already be written at this point.
        var entity = AssetEntity.builder()
                .id(insert.id().value())
                .homeRegion(insert.placement().homeRegion())
                .objectBucket(insert.placement().objectBucket())
                .directory(insert.directory())
                .entryPoint(insert.entryPoint())
                .extension(insert.extension())
                .contentType(insert.contentType())
                .bytes(insert.bytes())
                .deliveryProtocol(insert.deliveryProtocol())
                .status(AssetStatus.UPLOADING)
                .objects(List.copyOf(insert.objects()))
                .build();
        entityManager.persist(entity);
        entityManager.flush();
        return entity.toModel(null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findAsset(AssetId id) {
        return assetCrudRepository.findById(id.value()).map(entity -> entity.toModel(
                imageMetadataCrudRepository.findById(id.value()).orElse(null),
                videoMetadataCrudRepository.findById(id.value()).orElse(null)));
    }

    @Override
    @Transactional
    public void setImageMetadata(AssetId id, ImageMetadata metadata) {
        imageMetadataCrudRepository.saveAndFlush(ImageMetadataEntity.builder()
                .asset(assetCrudRepository.getReferenceById(id.value()))
                .resolutionX(metadata.width())
                .resolutionY(metadata.height())
                .build());
    }

    @Override
    @Transactional
    public void setVideoMetadata(AssetId id, VideoMetadata metadata) {
        videoMetadataCrudRepository.saveAndFlush(VideoMetadataEntity.builder()
                .asset(assetCrudRepository.getReferenceById(id.value()))
                .resolutionX(metadata.width())
                .resolutionY(metadata.height())
                .durationMilliseconds(Math.toIntExact(metadata.durationMs()))
                .build());
    }

    @Override
    @Transactional
    public void setStatus(Collection<AssetId> ids, AssetStatus status) {
        var values = ids.stream().map(AssetId::value).toList();
        if (assetCrudRepository.updateStatusAll(values, status) != values.size()) {
            throw new IllegalArgumentException("could not update the status of every asset");
        }
    }

    @Override
    @Transactional
    public void deleteAssets(Collection<AssetId> ids) {
        assetCrudRepository.deleteAll(ids.stream().map(AssetId::value).toList());
    }
}
