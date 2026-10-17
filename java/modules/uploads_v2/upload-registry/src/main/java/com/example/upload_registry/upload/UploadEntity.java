package com.example.upload_registry.upload;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.stored_asset.StoredAsset;
import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.UploadStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "upload")
@SuperBuilder
public final class UploadEntity {
    @Column(nullable = false, length = 100)
    private String uploadKey;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private StoredAssetEntity storedAsset;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private UploadStatus status;

    public Upload toModel() {
        return toModel(StoredAssetEntity.toModel(storedAsset, null, null));
    }

    public Upload toModelWithImageMetadata() {
        return toModel(StoredAssetEntity.toModel(storedAsset, storedAsset.getImageMetadata(), null));
    }

    public Upload toModelWithVideoMetadata() {
        return toModel(StoredAssetEntity.toModel(storedAsset, null, storedAsset.getVideoMetadata()));
    }

    private Upload toModel(StoredAsset asset) {
        return Upload.builder()
                .uploadKey(uploadKey)
                .id(id)
                .createdAt(createdAt)
                .status(status)
                .storedAsset(asset)
                .build();
    }
}
