package com.example.upload_registry.variant;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.uploads_api.variant.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@NullMarked
public class VariantRepositoryImpl implements VariantRepository {

    private final VariantCrudRepository variantCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public VariantId save(InsertVariant insert) {
        VariantEntityId entityId = null;
        if (insert.getId() != null) {
            entityId = new VariantEntityId(insert.getId().getUploadId(), insert.getId().getNamedTransformationId());
        }

        var variantEntity = VariantEntity.builder()
                .id(entityId)
                .status(insert.getStatus())
                .transformationVersionEntity(entityManager.getReference(TransformationVersionEntity.class, insert.getTransformationVersionId()))
                .storedAsset(entityManager.getReference(StoredAssetEntity.class, insert.getStoredAssetId()))
                .build();

        // For upsert on PK, JPA merge or saveAndFlush works. For @MapsId relationships, we set references.
        // If entity exists, update status (we added setter).
        if (entityId != null) {
            variantCrudRepository.findById(entityId).ifPresent(existing -> {
                existing.setStatus(insert.getStatus());
                variantEntity.setStatus(insert.getStatus()); // ensure
            });
        }

        var saved = variantCrudRepository.saveAndFlush(variantEntity);
        return new VariantId(saved.getId().getUploadId(), saved.getId().getNamedTransformationId());
    }

    @Override
    @Transactional
    public void updateStatus(VariantId variantId, VariantStatus status) {
        VariantEntityId id = new VariantEntityId(variantId.getUploadId(), variantId.getNamedTransformationId());
        int updated = variantCrudRepository.updateStatus(
                variantId.getUploadId(), variantId.getNamedTransformationId(), status);
        if (updated == 0) {
            throw new NoSuchElementException("No variant found for id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Variant findByUploadKeyAndTransformationName(String uploadKey, String transformationName) {
        var entity = variantCrudRepository.findByUploadKeyAndTransformationNameAndStatus(
                        uploadKey, transformationName, VariantStatus.READY)
                .orElseThrow(() -> new NoSuchElementException(
                        "No READY variant for uploadKey=" + uploadKey + ", transformation=" + transformationName));

        return toModel(entity);
    }

    @Override
    @Transactional
    public Optional<Variant> findForUpdate(VariantId variantId) {
        VariantEntityId id = new VariantEntityId(variantId.getUploadId(), variantId.getNamedTransformationId());
        var entityOpt = variantCrudRepository.findByIdForUpdate(id);
        entityOpt.ifPresent(entity -> entityManager.lock(entity, LockModeType.PESSIMISTIC_WRITE));
        return entityOpt.map(this::toModel);
    }

    @Override
    @Transactional
    public void delete(VariantId variantId) {
        VariantEntityId id = new VariantEntityId(variantId.getUploadId(), variantId.getNamedTransformationId());
        variantCrudRepository.deleteById(id);
    }

    private Variant toModel(VariantEntity entity) {
        return Variant.builder()
                .id(new VariantId(entity.getId().getUploadId(), entity.getId().getNamedTransformationId()))
                .transformationVersionId(entity.getTransformationVersionEntity().getId())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .storedAssetId(entity.getStoredAsset().getId())
                .build();
    }
}
