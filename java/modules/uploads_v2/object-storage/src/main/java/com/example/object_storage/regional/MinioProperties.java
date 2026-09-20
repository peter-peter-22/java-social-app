package com.example.object_storage.regional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "minio")
@Validated
record MinioProperties(
        @NotEmpty List<MinioRegion> regions,
        @NotBlank String homeRegion
) {
    public record MinioRegion(
            @NotBlank String name,
            @NotBlank URI endpoint,
            @NotBlank String accessKey,
            @NotBlank String secretKey
    ) {
    }
}