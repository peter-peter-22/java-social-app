package com.example.media.registry;

import org.hibernate.cfg.MappingSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The registry's jsonb columns serialize through {@link MediaJsonFormatMapper}, so the persisted
 * transformation specs speak exactly the JSON shape defined by {@code media-json}. Spring Boot
 * does not link its Jackson mapper to Hibernate, so the format mapper is installed here for every
 * application that scans this package.
 */
@Configuration
public class RegistryJsonConfiguration {

    @Bean
    HibernatePropertiesCustomizer mediaJsonFormatMapper() {
        return properties -> properties.put(MappingSettings.JSON_FORMAT_MAPPER, new MediaJsonFormatMapper());
    }
}