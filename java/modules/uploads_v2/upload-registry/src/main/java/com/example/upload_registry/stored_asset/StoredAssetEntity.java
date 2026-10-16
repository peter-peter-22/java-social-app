package com.example.upload_registry.stored_asset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(nullable = false, length = 50)
    private String objectBucket;
    @Column(nullable = false, length = 50)
    private String objectPath;
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
    private Integer bytes;
    @Column(length = 20)
    private String deliveryProtocol;

    @OneToOne(mappedBy = "storedAsset")
    private ImageMetadataEntity imageMetadata;
    @OneToOne(mappedBy = "storedAsset")
    private VideoMetadataEntity videoMetadata;
}
