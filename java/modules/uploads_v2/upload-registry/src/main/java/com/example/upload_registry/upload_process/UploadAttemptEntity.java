package com.example.upload_registry.upload_process;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.uploads.upload_process.UploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadAttemptStatus;
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
    @Setter
    private UploadProcessEntity uploadProcess;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar(50)[]")
    @Setter
    private @Nullable List<String> eagerTransformationNames;
    @Column(length = 50)
    @Setter
    private @Nullable String incomingTransformationName;
    /**
     * The original file from the upload request without incoming transformation.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_asset_id", nullable = false)
    @Setter
    private StoredAssetEntity originalAsset;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    /**
     * Initial status: PENDING
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private UploadAttemptStatus status;
    @Setter
    private @Nullable Instant finishedAt;

    public UploadAttempt toModel() {
        return UploadAttempt.builder()
                .attemptId(attemptId)
                .uploadKey(uploadProcess.getUploadKey())
                .eagerTransformationNames(eagerTransformationNames)
                .incomingTransformationName(incomingTransformationName)
                .originalAssetId(originalAsset.getId())
                .createdAt(createdAt)
                .status(status)
                .finishedAt(finishedAt)
                .build();
    }
}
