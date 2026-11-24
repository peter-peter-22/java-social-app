package com.example.media.imageworker.completion;

import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.json.MediaJson;
import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * The completion call back to the API travels over HTTP in the contract's JSON shape. The retry
 * policy is the same as the API's blocking worker call: transport failures and server errors are
 * retried, a 4xx answer is not.
 */
class ApiCompletionClientTest {

    private final RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl("http://api.test")
            // The result travels in the shared media JSON shape, like the production adapter.
            .configureMessageConverters(converters ->
                    converters.withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())));
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final RetryTemplate retryTemplate = new RetryTemplate(RetryPolicy.builder()
            .includes(ResourceAccessException.class, HttpServerErrorException.class)
            .maxRetries(2)
            .delay(Duration.ZERO)
            .build());
    private final ApiCompletionClient client = new ApiCompletionClient(restClientBuilder.build(), retryTemplate);

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final ImageTransformResult RESULT = new ImageTransformResult(ATTEMPT_ID, List.of(), null);

    @Test
    void reportsTheResult() {
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        assertThatCode(() -> client.complete(RESULT)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void retriesServerErrors() {
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        client.complete(RESULT);
        server.verify();
    }

    @Test
    void retriesTransportFailures() {
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException("connection reset")));
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        client.complete(RESULT);
        server.verify();
    }

    @Test
    void doesNotRetryClientErrors() {
        server.expect(requestTo("http://api.test/api/completion/image"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.complete(RESULT))
                .isInstanceOf(HttpClientErrorException.class);
        server.verify();
    }

    @Test
    void exhaustedServerErrorsPropagateToTheCaller() {
        for (int i = 0; i <= 2; i++) {
            server.expect(requestTo("http://api.test/api/completion/image"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withServerError());
        }

        assertThatThrownBy(() -> client.complete(RESULT))
                .isInstanceOf(HttpServerErrorException.class);
        server.verify();
    }
}
