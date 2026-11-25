package com.example.media.api;

import com.example.media.api.security.UploadPermissionProperties;
import com.example.media.api.worker.HttpImageTransformer;
import com.example.media.api.worker.RabbitImageTransformer;
import com.example.media.contract.port.AsyncImageTransformer;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.worker.FailureClassifier;
import com.example.media.contract.worker.MediaQueue;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

/**
 * The application services demarcate their transactions explicitly with {@link TransactionTemplate}:
 * the finalize commit must span several port calls while the object-store work stays outside the
 * transaction. The transaction manager comes with the persistence adapters.
 *
 * <p>The worker transports are driven adapters wired here: the blocking call is HTTP to the
 * worker's transform endpoint ({@link ImageTransformer}), the async call publishes the same command
 * to RabbitMQ ({@link AsyncImageTransformer}). Each port carries one transport, so the upload use
 * case depends on the narrow capability it needs and does not know the transport behind it.</p>
 *
 * <p>The security credentials are time-bound, so their services take one injectable {@link Clock}
 * instead of reading the wall clock: issuance, expiry, and the tests that assert them then share a
 * single instant source.</p>
 */
@Configuration
@EnableConfigurationProperties(UploadPermissionProperties.class)
@NullMarked
public class MediaApiConfiguration {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /** The instant source of the credential services. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * The blocking {@link ImageTransformer} the upload use case calls: the HTTP adapter to the
     * worker's transform endpoint.
     *
     * <p>The adapter retries the failures the shared {@link FailureClassifier} calls recoverable (it
     * marks transport failures and worker server errors); a 4xx answer is a command error and is not
     * retried. The worker allocates fresh asset ids on every execution, so retrying the same command
     * writes to new object keys.</p>
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
                .predicate(FailureClassifier::isRecoverable)
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

    /**
     * The async {@link AsyncImageTransformer} the upload use case calls: the RabbitMQ adapter that
     * publishes the same command to the worker queue and returns immediately. The worker later calls
     * the API completion endpoint, which finalizes the attempt.
     */
    @Bean
    AsyncImageTransformer asyncImageTransformer(RabbitTemplate rabbitTemplate) {
        return new RabbitImageTransformer(rabbitTemplate, MediaQueue.EXCHANGE, MediaQueue.COMMAND_ROUTING_KEY);
    }
}