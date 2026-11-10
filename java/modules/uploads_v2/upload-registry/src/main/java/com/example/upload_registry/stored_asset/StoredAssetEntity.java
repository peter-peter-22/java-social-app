package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.StoredAsset;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_asset")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class StoredAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 50)
    private String homeRegion;
    // TODO remove the bucket from the stored asset
    @Column(nullable = false, length = 50)
    private String objectBucket;
    @Column(nullable = false)
    private boolean directory;
    @Column()
    private String entryPoint;
    @Column(nullable = false, length = 50)
    private String mimeType;
    @Column(length = 20)
    private String extension;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    private Long bytes;
    @Column(length = 20)
    private String deliveryProtocol;

    @OneToOne(mappedBy = "storedAsset")
    private @Nullable ImageMetadataEntity imageMetadata;
    @OneToOne(mappedBy = "storedAsset")
    private VideoMetadataEntity videoMetadata;

    public static StoredAsset toModel(StoredAssetEntity entity) {
        return toModel(entity, entity.getImageMetadata(), entity.getVideoMetadata());
    }

    public static StoredAsset toModel(StoredAssetEntity entity, @Nullable ImageMetadataEntity imageMetadata,
                                      @Nullable VideoMetadataEntity videoMetadata) {
        return StoredAsset.builder()
                .id(entity.getId())
                .homeRegion(entity.getHomeRegion())
                .directory(entity.isDirectory())
                .entryPoint(entity.getEntryPoint())
                .mimeType(entity.getMimeType())
                .extension(entity.getExtension())
                .createdAt(entity.getCreatedAt())
                .bytes(entity.getBytes())
                .deliveryProtocol(entity.getDeliveryProtocol())
                .imageMetadata(imageMetadata == null ? null : ImageMetadataEntity.toModel(imageMetadata))
                .videoMetadata(videoMetadata == null ? null : VideoMetadataEntity.toModel(videoMetadata))
                .build();
    }
}
