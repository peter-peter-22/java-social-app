package com.example.media.api;

import com.example.media.api.rabbit.MediaQueue;
import com.example.media.api.worker.HttpImageTransformer;
import com.example.media.api.worker.RabbitImageTransformer;
import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.worker.FailureClassifier;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.Duration;

/**
 * The application services demarcate their transactions explicitly with {@link TransactionTemplate}:
 * the finalize commit must span several port calls while the object-store work stays outside the
 * transaction. The transaction manager comes with the persistence adapters.
 *
 * <p>The worker transports are adapters wired here: the blocking call is HTTP to the worker's
 * transform endpoint, the async call publishes the same command to RabbitMQ. The single
 * {@link ImageTransformer} bean dispatches between them, so the upload use case does not know which
 * transport its request uses.</p>
 */
@Configuration
@NullMarked
public class MediaApiConfiguration {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /**
     * The {@link ImageTransformer} the upload use case calls: the blocking HTTP adapter and the
     * async RabbitMQ adapter behind one port, chosen by the request.
     *
     * <p>The blocking adapter retries the failures the shared {@link FailureClassifier} calls
     * recoverable (the adapter marks transport failures and worker server errors); a 4xx answer is
     * a command error and is not retried. The worker allocates fresh asset ids on every execution,
     * so retrying the same command writes to new object keys.</p>
     */
    @Bean
    ImageTransformer imageTransformer(
            RabbitTemplate rabbitTemplate,
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

        var blocking = new HttpImageTransformer(restClient, new RetryTemplate(retryPolicy));
        var async = new RabbitImageTransformer(rabbitTemplate, MediaQueue.EXCHANGE, MediaQueue.COMMAND_ROUTING_KEY);
        return new ImageTransformer() {
            @Override
            public ImageTransformResult transform(ImageTransformCommand command) {
                return blocking.transform(command);
            }

            @Override
            public void transformAsync(ImageTransformCommand command) {
                async.transformAsync(command);
            }
        };
    }
}