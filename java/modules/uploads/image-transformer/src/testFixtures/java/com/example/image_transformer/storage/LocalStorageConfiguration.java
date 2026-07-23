package com.example.image_transformer.storage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class LocalStorageConfiguration {
    @Bean
    FileStreamStorage fileStreamStorage() {
        return new LocalStreamStorage();
    }
}
