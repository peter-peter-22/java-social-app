package com.example.media.api;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The application services demarcate their transactions explicitly with {@link TransactionTemplate}:
 * the finalize commit must span several port calls while the object-store work stays outside the
 * transaction. The transaction manager comes with the persistence adapters.
 */
@Configuration
@NullMarked
public class MediaApiConfiguration {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}