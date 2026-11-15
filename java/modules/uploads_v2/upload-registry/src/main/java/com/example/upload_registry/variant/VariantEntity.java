package com.example.upload_registry.variant;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.transformations.NamedTransformationEntity;
import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.upload_registry.upload.UploadEntity;
import com.example.uploads_api.variant.Variant;
import com.example.uploads_api.variant.VariantId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "variant")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class VariantEntity {
    @EmbeddedId
    private VariantEntityId id;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @MapsId("uploadId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    private UploadEntity uploadEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformation_version_id")
    private TransformationVersionEntity transformationVersionEntity;
    @MapsId("namedTransformationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "named_transformation_id")
    private NamedTransformationEntity namedTransformationEntity;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private StoredAssetEntity storedAsset;

    public Variant toModel() {
        return Variant.builder()
                .id(VariantId.builder()
                        .uploadId(id.getUploadId())
                        .namedTransformationId(id.getNamedTransformationId())
                        .build())
                .transformationVersionId(transformationVersionEntity.getId())
                .createdAt(createdAt)
                .storedAssetId(storedAsset.getId())
                .build();
    }
}
