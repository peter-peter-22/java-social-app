package com.example.upload_registry.variant;

import com.example.upload_registry.stored_asset.StoredAssetCrudRepository;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.transformations.NamedTransformationEntity;
import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.upload_registry.upload.UploadEntity;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import com.example.uploads_api.variant.InsertVariant;
import com.example.uploads_api.variant.Variant;
import com.example.uploads_api.variant.VariantId;
import com.example.uploads_api.variant.VariantRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Repository
@RequiredArgsConstructor
@NullMarked
public class VariantRepositoryImpl implements VariantRepository {
    private final VariantCrudRepository variantCrudRepository;
    private final StoredAssetCrudRepository storedAssetCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public VariantId insert(InsertVariant insert) {
        try {
            return variantCrudRepository.saveAndFlush(VariantEntity.builder()
                            .id(new VariantEntityId(insert.getId().getUploadId(), insert.getId().getNamedTransformationId()))
                            .uploadEntity(entityManager.getReference(UploadEntity.class, insert.getId().getUploadId()))
                            .namedTransformationEntity(entityManager.getReference(
                                    NamedTransformationEntity.class, insert.getId().getNamedTransformationId()))
                            .transformationVersionEntity(entityManager.getReference(
                                    TransformationVersionEntity.class, insert.getTransformationVersionId()))
                            .storedAsset(entityManager.getReference(StoredAssetEntity.class, insert.getStoredAssetId()))
                            .build())
                    .getId()
                    .toModel();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Variant already exists for " + insert.getId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Variant findByUploadKeyAndTransformationName(String uploadKey, String transformationName) {
        return variantCrudRepository.findByUploadKeyAndTransformationName(uploadKey, transformationName)
                .map(VariantEntity::toModel)
                .orElseThrow();
    }

    @Override
    @Transactional
    public void delete(VariantId variantId) {
        var entityId = new VariantEntityId(variantId.getUploadId(), variantId.getNamedTransformationId());
        var variant = variantCrudRepository.findById(entityId)
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        storedAssetCrudRepository.updateStatusAll(
                java.util.List.of(variant.getStoredAsset().getId()), StoredAssetStatus.DELETE);
        variantCrudRepository.delete(variant);
        variantCrudRepository.flush();
    }
}
