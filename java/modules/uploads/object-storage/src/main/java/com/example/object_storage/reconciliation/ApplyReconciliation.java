package com.example.object_storage.reconciliation;

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
    private final ReconciliationManager reconciliationManager;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        reconciliationManager.applyReconciliation();
    }
}
