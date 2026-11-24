package com.example.media.imageworker;

import com.example.media.imageworker.completion.ApiCompletionClient;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * The outbound adapters of the worker. The API completion call is an HTTP adapter wired here to the
 * API's base URL; it uses the same retry policy as the API's blocking worker call, so the
 * recoverability classification is the same.
 */
@Configuration
@NullMarked
public class WorkerConfiguration {

    @Bean
    ApiCompletionClient apiCompletionClient(
            @Value("${media.api.base-url}") String apiBaseUrl,
            @Value("${media.api.max-retries:3}") long maxRetries,
            @Value("${media.api.retry-delay-ms:200}") long retryDelayMs,
            @Value("${media.api.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${media.api.read-timeout-ms:30000}") long readTimeoutMs
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
                .baseUrl(apiBaseUrl)
                .requestFactory(requestFactory)
                // The result travels in the contract's JSON shape, like the worker's HTTP endpoint.
                .configureMessageConverters(converters ->
                        converters.withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())))
                .build();
        return new ApiCompletionClient(restClient, new RetryTemplate(retryPolicy));
    }
}
