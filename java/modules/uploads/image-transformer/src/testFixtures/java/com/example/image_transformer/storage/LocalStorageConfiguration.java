package com.example.image_transformer.storage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class LocalStorageConfiguration {
    @Bean
    @Primary
    FileStreamStorage fileStreamStorage() {
        return new LocalStreamStorage();
    }
}
