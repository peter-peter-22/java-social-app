package com.example.media.registry.asset;

import com.example.media.contract.asset.ImageMetadata;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

/**
 * The {@code image_metadata} table: the recorded dimensions of an image asset.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "image_metadata")
public final class ImageMetadataEntity {
    @Id
    private UUID assetId;
    @Column(nullable = false, name = "resolution_x")
    private Integer resolutionX;
    @Column(nullable = false, name = "resolution_y")
    private Integer resolutionY;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AssetEntity asset;

    public ImageMetadata toModel() {
        return new ImageMetadata(resolutionX, resolutionY);
    }
}
