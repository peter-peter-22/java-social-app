package com.example.object_storage.regional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "minio")
@Validated
public record MinioProperties(
        @NotBlank String homeRegion,
        @NotNull ReconciliationConfiguration reconciliation,
        @NotNull UploadsConfiguration uploadsConfiguration,
        @NotEmpty List<MinioRegion> regions
) {
    public record MinioRegion(
            @NotBlank String name,
            @NotBlank URI endpoint,
            @NotBlank String accessKey,
            @NotBlank String secretKey
    ) {
    }

    public record ReconciliationConfiguration(
            @NotNull boolean enabled
    ) {
    }

    /**
     * @param expirationDays This controls the max lifespan of the temporal objects and the uploads.
     *                       Days are the smallest unit Minio can set for object expiration and upload expiration.
     */
    public record UploadsConfiguration(
            @NotNull String bucketName,
            @NotNull int expirationDays
    ) {
    }
}