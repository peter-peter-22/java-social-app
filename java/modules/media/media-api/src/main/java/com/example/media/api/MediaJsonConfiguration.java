package com.example.media.api;

import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The API's HTTP JSON speaks the media service's standard shape: the mixins of {@link MediaJson}
 * are applied to the auto-configured Jackson mapper, so the worker messages carry the same
 * polymorphic transformation specs the registry persists.
 */
@Configuration
@NullMarked
public class MediaJsonConfiguration {

    @Bean
    JsonMapperBuilderCustomizer mediaJson() {
        return MediaJson::customize;
    }
}