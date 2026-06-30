package com.example.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "minio.reconciliation")
@Validated
record ReconciliationProperties (
        @DefaultValue("false") @NotNull boolean enabled
){}
