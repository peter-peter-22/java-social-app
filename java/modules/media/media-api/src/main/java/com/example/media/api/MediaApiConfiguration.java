package com.example.media.api;

import com.example.media.api.worker.HttpImageTransformer;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

/**
 * The application services demarcate their transactions explicitly with {@link TransactionTemplate}:
 * the finalize commit must span several port calls while the object-store work stays outside the
 * transaction. The transaction manager comes with the persistence adapters. The blocking worker call
 * is an HTTP adapter wired here to the worker's base URL.
 */
@Configuration
@NullMarked
public class MediaApiConfiguration {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /** The blocking {@link ImageTransformer} port over HTTP, targeting one image worker. */
    @Bean
    ImageTransformer imageTransformer(@Value("${media.worker.base-url}") String workerBaseUrl) {
        RestClient restClient = RestClient.builder()
                .baseUrl(workerBaseUrl)
                // The command carries contract specs: the client speaks the shared media JSON shape.
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())))
                .build();
        return new HttpImageTransformer(restClient);
    }
}