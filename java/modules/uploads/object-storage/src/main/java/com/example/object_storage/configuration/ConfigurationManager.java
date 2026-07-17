package com.example.object_storage.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ConfigurationManager {
    private final MinioConfiguration[] configurations;

    public void applyReconciliation() throws Exception {
        System.out.printf("Applying %d configurations%n", configurations.length);
        for (var configuration : configurations) {
            try{
                System.out.println("Applying configuration: " + configuration.getName());
                configuration.apply();
                System.out.println("Applied configuration: " + configuration.getName());
            }
           catch (Exception e) {
                System.out.println("Failed to apply configuration: " + configuration.getName());
                throw e;
           }
        }
        System.out.printf("Applied %d configurations%n", configurations.length);
    }
}
