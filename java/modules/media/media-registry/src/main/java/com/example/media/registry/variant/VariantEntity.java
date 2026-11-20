package com.example.media.registry.variant;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.transformation.NamedTransformationId;
import com.example.media.contract.transformation.TransformationVersionId;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.variant.Variant;
import com.example.media.contract.variant.VariantId;
import com.example.media.registry.asset.AssetEntity;
import com.example.media.registry.transformation.NamedTransformationEntity;
import com.example.media.registry.transformation.TransformationVersionEntity;
import com.example.media.registry.upload.UploadEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * The {@code variant} table: a named transformed result of an upload.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "variant")
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
    private AssetEntity asset;

    public Variant toModel() {
        return new Variant(
                new VariantId(
                        new UploadId(id.getUploadId()),
                        new NamedTransformationId(id.getNamedTransformationId())),
                new TransformationVersionId(transformationVersionEntity.getId()),
                new AssetId(asset.getId()),
                createdAt
        );
    }
}
