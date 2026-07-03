package com.example.media.object_storage;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "minio")
@Validated
public record MinioProperties (
        @NotNull URI endpoint,
        @NotNull String accessKey,
        @NotNull String secretKey
){}