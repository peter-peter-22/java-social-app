package com.example.media.object_storage.configuration;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableConfigurationProperties(ReconciliationProperties.class)
public class ApplyReconciliation implements ApplicationRunner {
    private final ConfigurationManager configurationManager;

    @Override
    public void run(@NotNull ApplicationArguments args) throws Exception {
        configurationManager.applyReconciliation();
    }
}
