package com.example.upload_registry.variant;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.upload_registry.upload.UploadEntity;
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
    private VariantId id;
    @MapsId("uploadId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    private UploadEntity uploadEntity;
    @MapsId("transformationVersionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformation_version_id")
    private TransformationVersionEntity transformationVersionEntity;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private StoredAssetEntity storedAsset;
}
