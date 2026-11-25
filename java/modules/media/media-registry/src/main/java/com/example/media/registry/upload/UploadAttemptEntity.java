package com.example.media.registry.upload;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.UploadAttempt;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import com.example.media.registry.asset.AssetEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The {@code upload_attempt} table: one try at producing the final upload of a process.
 */
@Entity
@Table(name = "upload_attempt")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class UploadAttemptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID attemptId;
    /**
     * The upload key also marks the primary key of the upload process.
     * The attempt depends on the process and must be cleaned up when it's deleted.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_key", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UploadProcessEntity uploadProcess;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar(50)[]")
    private @Nullable List<String> eagerTransformationNames;
    @Column(length = 50)
    private @Nullable String incomingTransformationName;
    /** How the resulting upload is read. Recorded here so async completion knows it. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadVisibility visibility;
    /**
     * The permission this attempt consumed, if any. A terminal failure revokes its remaining uses.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_permission_id")
    private @Nullable UploadPermissionEntity uploadPermission;
    /**
     * The original file from the upload request without incoming transformation.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_asset_id", nullable = false)
    private AssetEntity originalAsset;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private UploadAttemptStatus status;
    @Setter
    private @Nullable Instant finishedAt;

    public UploadAttempt toModel() {
        return new UploadAttempt(
                new AttemptId(attemptId),
                new UploadKey(uploadProcess.getUploadKey()),
                new AssetId(originalAsset.getId()),
                eagerTransformationNames == null ? List.of() : List.copyOf(eagerTransformationNames),
                incomingTransformationName,
                visibility,
                status,
                createdAt,
                finishedAt
        );
    }
}
