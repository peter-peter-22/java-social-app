package com.example.media.registry.upload;

import com.example.media.contract.upload.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The {@code upload_permission} table: the row behind one issued upload token.
 * Only the token hash is stored. The constraint columns are authoritative for the upload that
 * consumes the permission.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "upload_permission")
public final class UploadPermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 64, unique = true)
    private String tokenHash;
    @Column(nullable = false, length = 100)
    private String uploadKey;
    @Column(nullable = false, length = 20)
    private String extension;
    @Column(length = 127)
    private @Nullable String contentType;
    private @Nullable Long maxBytes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar(50)[]")
    private @Nullable List<String> eagerTransformationNames;
    @Column(length = 50)
    private @Nullable String incomingTransformationName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadVisibility visibility;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private int maxUses;
    @Column(nullable = false)
    @Setter
    private int uses;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private UploadPermissionStatus status;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    public UploadPermission toModel() {
        return new UploadPermission(
                new UploadPermissionId(id),
                tokenHash,
                new UploadKey(uploadKey),
                extension,
                contentType,
                maxBytes,
                eagerTransformationNames == null ? List.of() : List.copyOf(eagerTransformationNames),
                incomingTransformationName,
                visibility,
                expiresAt,
                maxUses,
                uses,
                status,
                createdAt
        );
    }
}
