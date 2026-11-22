package com.example.object_storage.reconciliation;

import com.example.object_storage.regional.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableConfigurationProperties(MinioProperties.class)
class ApplyReconciliation implements ApplicationRunner {
    private final ReconciliationManager reconciliationManager;
    private final MinioProperties minioProperties;

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        if (minioProperties.reconciliation().enabled())
            reconciliationManager.applyReconciliation();
    }
}
