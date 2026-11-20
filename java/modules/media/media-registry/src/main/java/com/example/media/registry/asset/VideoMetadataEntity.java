package com.example.media.registry.asset;

import com.example.media.contract.asset.VideoMetadata;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

/**
 * The {@code video_metadata} table: the recorded dimensions and duration of a video asset.
 */
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AssetEntity asset;

    public VideoMetadata toModel() {
        return new VideoMetadata(resolutionX, resolutionY, durationMilliseconds);
    }
}
