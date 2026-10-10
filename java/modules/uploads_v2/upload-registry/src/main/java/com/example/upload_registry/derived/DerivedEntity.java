package com.example.upload_registry.derived;

import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.upload_registry.upload.UploadEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "derived")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class DerivedEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploadId")
    private UploadEntity uploadEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformationVersionId")
    private TransformationVersionEntity transformationVersionEntity;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}
