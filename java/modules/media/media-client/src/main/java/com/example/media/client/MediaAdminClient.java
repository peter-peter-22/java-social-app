package com.example.media.client;

import com.example.media.contract.transformation.TransformationSpec;
import com.example.media.contract.upload.UploadVisibility;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

/**
 * The admin facade of the media service client: defines named transformations and issues upload
 * permissions. The wire format is the shared media JSON shape ({@code media-json}), so the specs
 * reach the registry exactly as a browser client sends them. The admin key travels in the
 * {@code X-Admin-Key} header.
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

    /**
     * The body of an upload permission request: the constraints of the upload it authorizes. A
     * {@code null} upload key, TTL, use budget, or visibility takes the server's default.
     */
    public record NewUploadPermission(
            @Nullable String uploadKey,
            String extension,
            @Nullable String contentType,
            @Nullable Long maxBytes,
            @Nullable List<String> eagerTransformations,
            @Nullable String incomingTransformation,
            @Nullable UploadVisibility visibility,
            @Nullable Long expiresInSeconds,
            @Nullable Integer maxUses
    ) {
    }

    /**
     * An issued upload permission. {@code permission} is the raw bearer token; it is returned by the
     * server exactly once and must be stored by the caller.
     */
    public record IssuedUploadPermission(
            String permissionId,
            String uploadKey,
            String permission,
            Instant expiresAt,
            int maxUses
    ) {
    }

    /**
     * The body of a download permission request: the upload to authorize and an optional lifetime.
     * A {@code null} TTL takes the server's default.
     */
    public record NewDownloadPermission(
            String uploadKey,
            @Nullable Long expiresInSeconds
    ) {
    }

    /**
     * An issued download permission: the signed token for one private upload version, together with
     * the {@code v}, {@code e}, {@code s}, and {@code k} query parameters of a read URL.
     */
    public record IssuedDownloadPermission(
            String uploadKey,
            String version,
            Instant expiresAt,
            long expires,
            String signature,
            String keyId,
            String pathPrefix
    ) {
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

    /**
     * Issues an upload permission: the bearer token a client presents to start the upload whose
     * constraints the request describes. The token is returned once, in the answer.
     */
    public IssuedUploadPermission createUploadPermission(NewUploadPermission request) {
        return restClient.post()
                .uri("/api/admin/upload-permission")
                .header("X-Admin-Key", adminKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(IssuedUploadPermission.class);
    }

    /**
     * Issues a download permission: the signed token a client presents to read one private upload
     * version. The token is stateless and valid until it expires.
     */
    public IssuedDownloadPermission createDownloadPermission(NewDownloadPermission request) {
        return restClient.post()
                .uri("/api/admin/download-permission")
                .header("X-Admin-Key", adminKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(IssuedDownloadPermission.class);
    }
}
