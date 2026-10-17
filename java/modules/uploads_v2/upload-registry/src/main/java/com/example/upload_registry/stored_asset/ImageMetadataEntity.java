package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.ImageMetadata;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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
    private StoredAssetEntity storedAsset;

    public static ImageMetadata toModel(ImageMetadataEntity imageMetadata) {
        return ImageMetadata.builder()
                .resolutionX(imageMetadata.getResolutionX())
                .resolutionY(imageMetadata.getResolutionY())
                .build();
    }
}
