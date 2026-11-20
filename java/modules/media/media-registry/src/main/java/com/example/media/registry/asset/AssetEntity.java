package com.example.media.registry.asset;

import com.example.media.contract.asset.*;
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

/**
 * The {@code stored_asset} table: one row per registered asset.
 * The id is assigned by the caller: it is the object prefix of the asset.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "stored_asset")
public final class AssetEntity {
    /**
     * Reserved by the caller before any object is written; the id is the object prefix.
     */
    @Id
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
    private @Nullable String extension;
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
    private AssetStatus status;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "varchar(255)[]")
    private List<String> objects;

    /**
     * The metadata rows are read with their own queries instead of the mapped one-to-one
     * associations, so mapping never triggers lazy loads.
     */
    public Asset toModel(@Nullable ImageMetadataEntity image, @Nullable VideoMetadataEntity video) {
        return new Asset(
                new AssetId(id),
                new AssetPlacement(homeRegion, objectBucket),
                status,
                deliveryProtocol,
                contentType,
                entryPoint,
                extension,
                directory,
                bytes,
                objects,
                image == null ? null : image.toModel(),
                video == null ? null : video.toModel(),
                createdAt
        );
    }
}
