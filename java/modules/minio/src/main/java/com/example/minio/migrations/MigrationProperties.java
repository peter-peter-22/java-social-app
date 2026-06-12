package com.example.minio.migrations;

import com.example.minio.PropertyContants;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = PropertyContants.MIGRATIONS_PREFIX)
record MigrationProperties(
        @NotNull @DefaultValue("false") Boolean applyOnStartup
) {
}
