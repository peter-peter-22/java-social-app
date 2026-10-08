package com.example.upload_registry.upload;

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
public class ImageMetadataEntity {
    @Id
    private UUID uploadId;
    @Column(nullable = false, name = "resolution_x")
    private Integer resolutionX;
    @Column(nullable = false, name = "resolution_y")
    private Integer resolutionY;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploadId")
    private UploadEntity upload;
}
