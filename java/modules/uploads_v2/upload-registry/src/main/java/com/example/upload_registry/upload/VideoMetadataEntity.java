package com.example.upload_registry.upload;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "video_metadata")
public final class VideoMetadataEntity {
    @Id
    private UUID assetId;
    @Column(nullable = false, name = "resolution_x")
    private Integer resolutionX;
    @Column(nullable = false, name = "resolution_y")
    private Integer resolutionY;
    @Column(nullable = false, name = "duration_seconds")
    private Double durationSeconds;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private StoredAssetEntity storedAsset;
}
