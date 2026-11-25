package com.example.media.registry.upload;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import com.example.media.registry.asset.AssetEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The {@code upload} table: the durable result of a successful attempt.
 */
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadVisibility visibility;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    public Upload toModel() {
        return new Upload(
                new UploadId(id),
                new UploadKey(uploadKey),
                new AssetId(asset.getId()),
                visibility,
                createdAt
        );
    }
}
