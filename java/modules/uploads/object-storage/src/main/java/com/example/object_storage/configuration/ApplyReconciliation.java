package com.example.object_storage.configuration;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableConfigurationProperties(ReconciliationProperties.class)
@ConditionalOnProperty(name = "minio.reconciliation.enabled", havingValue = "true")
class ApplyReconciliation implements ApplicationRunner {
    private final ConfigurationManager configurationManager;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        configurationManager.applyReconciliation();
    }
}
