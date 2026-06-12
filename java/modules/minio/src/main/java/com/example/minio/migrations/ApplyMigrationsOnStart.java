package com.example.minio.migrations;

import com.example.minio.PropertyContants;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MigrationProperties.class)
@ConditionalOnProperty(prefix = PropertyContants.MIGRATIONS_PREFIX, name = "apply-on-startup", havingValue = "true")
public class ApplyMigrationsOnStart {
    @Bean
    public MigrationManager migrationManager(@Autowired MinioClient minioClient) {
        return new MigrationManager(minioClient);
    }

    @Bean
    public ApplicationRunner applyMigrationsOnStart(MigrationManager mm) {
        return args -> mm.applyMigrations();
    }
}
