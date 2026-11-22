package com.example.media.api;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The composition root for the API's driven adapters. The JPA registry's entities and repositories
 * are enabled here, and the adapter beans are scanned in: the registry in
 * {@code com.example.media.registry} and the Minio object store in {@code com.example.object_storage}.
 * This lives in a plain {@link Configuration}, not on {@link Application}, so that web-slice tests
 * (which do not auto-configure JPA) do not try to enable the repositories or need a database.
 */
@Configuration
@NullMarked
@ComponentScan(basePackages = {"com.example.media.registry", "com.example.object_storage"})
@EntityScan("com.example.media.registry")
@EnableJpaRepositories("com.example.media.registry")
public class MediaPersistenceConfiguration {
}
