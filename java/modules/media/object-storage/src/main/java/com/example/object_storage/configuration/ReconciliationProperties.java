package com.example.object_storage.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "minio.reconciliation")
@Validated
record ReconciliationProperties (
        @NotNull boolean enabled
){}
