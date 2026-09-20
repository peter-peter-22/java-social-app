package com.example.object_storage.reconciliation;

import com.example.object_storage.regional.MinioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class ReconciliationManager {
    private final MinioReconciliation[] configurations;
    private final MinioConfig minioConfig;

    public void applyReconciliation() throws Exception {
        System.out.printf("Applying %d configurations%n", configurations.length);
        for (var region : minioConfig.getAllRegions()) {
            log.info("Configuring region {}", region);
            var client = minioConfig.getClient(region);
            for (var configuration : configurations) {
                try {
                    log.info("Applying configuration: {}", configuration.getName());
                    configuration.apply(client);
                    log.info("Applied configuration: {}", configuration.getName());
                } catch (Exception e) {
                    log.error("Failed to apply configuration: {}", configuration.getName());
                    throw e;
                }
            }
        }
        log.info("Applied {} configurations", configurations.length);
    }
}
