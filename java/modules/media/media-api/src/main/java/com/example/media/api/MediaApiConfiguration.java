package com.example.media.api;

import com.example.media.api.worker.HttpImageTransformer;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

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

    /**
     * The blocking {@link ImageTransformer} port over HTTP, targeting one image worker.
     *
     * <p>The worker allocates fresh asset ids on every execution, so retrying the same command is
     * safe. The adapter retries transport failures and server errors; a 4xx answer is a command
     * error and is not retried.</p>
     */
    @Bean
    ImageTransformer imageTransformer(
            @Value("${media.worker.base-url}") String workerBaseUrl,
            @Value("${media.worker.max-retries:3}") long maxRetries,
            @Value("${media.worker.retry-delay-ms:200}") long retryDelayMs,
            @Value("${media.worker.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${media.worker.read-timeout-ms:30000}") long readTimeoutMs
    ) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        var retryPolicy = RetryPolicy.builder()
                .includes(ResourceAccessException.class, HttpServerErrorException.class)
                .maxRetries(maxRetries)
                .delay(Duration.ofMillis(retryDelayMs))
                .multiplier(2)
                .maxDelay(Duration.ofSeconds(5))
                .build();

        RestClient restClient = RestClient.builder()
                .baseUrl(workerBaseUrl)
                .requestFactory(requestFactory)
                // The command carries contract specs: the client speaks the shared media JSON shape.
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())))
                .build();
        return new HttpImageTransformer(restClient, new RetryTemplate(retryPolicy));
    }
}