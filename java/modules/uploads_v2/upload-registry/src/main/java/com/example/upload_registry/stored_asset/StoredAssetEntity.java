package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
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
    @Column(nullable = false, length = 50)
    private String objectBucket;
    @Column(nullable = false)
    private boolean directory;
    @Column(nullable = false)
    private String entryPoint;
    @Column(length = 20)
    private String extension;
    @Column(nullable = false, length = 127)
    private String contentType;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    private Long bytes;
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AssetDeliveryProtocol deliveryProtocol;
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StoredAssetStatus status;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "varchar(255)[]")
    private List<String> objects;
    @OneToOne(mappedBy = "storedAsset")
    private @Nullable ImageMetadataEntity imageMetadata;
    @OneToOne(mappedBy = "storedAsset")
    private @Nullable VideoMetadataEntity videoMetadata;

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
                .objectBucket(entity.getObjectBucket())
                .extension(entity.getExtension())
                .contentType(entity.getContentType())
                .createdAt(entity.getCreatedAt())
                .bytes(entity.getBytes())
                .deliveryProtocol(entity.getDeliveryProtocol())
                .status(entity.getStatus())
                .objects(entity.getObjects())
                .imageMetadata(imageMetadata == null ? null : ImageMetadataEntity.toModel(imageMetadata))
                .videoMetadata(videoMetadata == null ? null : VideoMetadataEntity.toModel(videoMetadata))
                .build();
    }
}
