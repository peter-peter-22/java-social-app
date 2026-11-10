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
     * @param expirationDays This controls how long the uploads can wait before being finalized.
     *                            Both successful and ongoing uploads are marked for removal after this duration.
     */
    public record UploadsConfiguration(
            @NotNull String bucketName,
            @NotNull int expirationDays
    ) {
    }
}