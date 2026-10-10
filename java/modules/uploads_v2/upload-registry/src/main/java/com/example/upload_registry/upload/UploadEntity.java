package com.example.upload_registry.upload;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(nullable = false)
    private String homeRegion;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 10)
    private String mimeType;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    private Integer bytes;

    @OneToOne(mappedBy = "upload")
    ImageMetadataEntity imageMetadata;
}
