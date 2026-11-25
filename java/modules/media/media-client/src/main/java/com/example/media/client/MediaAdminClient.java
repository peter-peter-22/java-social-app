package com.example.media.client;

import com.example.media.contract.transformation.TransformationSpec;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * The admin facade of the media service client: defines named transformations and (later) creates
 * upload signatures and runs signature-less admin uploads. The wire format is the shared media JSON
 * shape ({@code media-json}), so the specs reach the registry exactly as a browser client sends
 * them. The admin key travels in the {@code X-Admin-Key} header.
 *
 * <p>Uses the shared Spring {@link RestClient}, built from the given builder so tests can bind a
 * {@code MockRestServiceServer} to it. Error answers are thrown as
 * {@link org.springframework.web.client.RestClientResponseException} by the standard RestClient
 * behavior.</p>
 */
@NullMarked
public final class MediaAdminClient {

    /** The body of a transformation definition: the name and the resolved spec. */
    public record NewTransformation(String name, TransformationSpec spec) {
    }

    /** The registered named transformation at its current version. */
    public record RegisteredTransformation(String name, TransformationSpec spec) {
    }

    private final RestClient restClient;
    private final String adminKey;

    public MediaAdminClient(RestClient.Builder builder, String baseUrl, String adminKey) {
        this.adminKey = adminKey;
        this.restClient = builder
                .baseUrl(baseUrl)
                // The specs travel in the shared media JSON shape, like the production adapters.
                .configureMessageConverters(converters -> converters
                        .registerDefaults()
                        .withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())))
                .build();
    }

    /** Registers a named transformation or records a new version of it. */
    public RegisteredTransformation registerTransformation(String name, TransformationSpec spec) {
        return restClient.post()
                .uri("/api/admin/transformation")
                .header("X-Admin-Key", adminKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NewTransformation(name, spec))
                .retrieve()
                .body(RegisteredTransformation.class);
    }
}
