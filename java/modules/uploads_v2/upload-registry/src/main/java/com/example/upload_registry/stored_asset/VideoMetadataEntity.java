package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.VideoMetadata;
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
    @Column(nullable = false, name = "duration_milliseconds")
    private Integer durationMilliseconds;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private StoredAssetEntity storedAsset;

    public static VideoMetadata toModel(VideoMetadataEntity videoMetadata) {
        return VideoMetadata.builder()
                .resolutionX(videoMetadata.getResolutionX())
                .resolutionY(videoMetadata.getResolutionY())
                .durationMilliseconds(videoMetadata.getDurationMilliseconds())
                .build();
    }
}
