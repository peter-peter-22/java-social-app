package com.example.media.client;

import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.json.MediaJson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The client facade of the media service web API: uploads images, reads originals and variants, and
 * polls the status of queued uploads. This is what the end-to-end test drives the stack with. The
 * wire format is the shared media JSON shape ({@code media-json}).
 *
 * <p>Uses the shared Spring {@link RestClient}. Error answers are thrown as
 * {@link org.springframework.web.client.RestClientResponseException} by the standard RestClient
 * behavior; a missing upload on read is therefore a 404 exception.</p>
 */
@NullMarked
public final class MediaClient {

    /**
     * The JSON body part of an image upload; the same shape as the server's request DTO.
     * {@code permission} is the raw token the admin issued for this upload: when it is present, its
     * stored constraints are authoritative and the request must match them.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UploadImageRequest(
            @Nullable String permission,
            @Nullable String uploadKey,
            @Nullable String extension,
            @Nullable String desiredContentType,
            @Nullable Integer desiredBytes,
            @Nullable List<String> eagerTransformations,
            @Nullable String incomingTransformation,
            @Nullable Boolean async
    ) {
    }

    /** The accepted upload: {@code async} is true when the transformations are queued. */
    public record UploadResult(String uploadKey, Instant createdAt, boolean async, @Nullable String version) {
    }

    /** The status of the key's active upload attempt. */
    public record UploadStatus(String uploadKey, UploadAttemptStatus status, @Nullable String version) {
    }

    /**
     * The download credential for a private read: the {@code e}, {@code s}, and {@code k} query
     * parameters. The version travels separately as {@code v}.
     */
    public record DownloadCredential(long expires, String signature, String keyId) {
    }

    /** A downloaded file: the content type and the stored bytes. */
    public record DownloadedImage(String contentType, String fileName, byte[] content) {
    }

    private final RestClient restClient;
    private final JsonMapper jsonMapper = MediaJson.create();

    public MediaClient(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                // The bodies travel in the shared media JSON shape, like the production adapters;
                // the defaults add the byte-array and multipart converters the reads and uploads use.
                .configureMessageConverters(converters -> converters
                        .registerDefaults()
                        .withJsonConverter(new JacksonJsonHttpMessageConverter(MediaJson.create())))
                .build();
    }

    /** Uploads an image file with its JSON body part; blocking uploads are finalized when this returns. */
    public UploadResult uploadImage(
            UploadImageRequest request,
            byte[] content,
            String fileName,
            String contentType
    ) {
        var jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        var fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("body", new HttpEntity<>(writeJson(request), jsonHeaders));
        parts.add("file", new HttpEntity<>(new NamedByteArrayResource(content, fileName), fileHeaders));

        return restClient.post()
                .uri("/api/upload/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(UploadResult.class);
    }

    /** Reads the original file of a public upload. The extension is cosmetic, like on the server. */
    public DownloadedImage readOriginal(String uploadKey, String extension) {
        return readOriginal(uploadKey, extension, null, null);
    }

    /**
     * Reads the original file, optionally with the cache version and download credential a private
     * upload requires. A public read ignores them; {@code version} is then only a cache key.
     */
    public DownloadedImage readOriginal(
            String uploadKey,
            String extension,
            @Nullable String version,
            @Nullable DownloadCredential credential
    ) {
        return read("/api/get/image/%s.%s".formatted(uploadKey, extension), version, credential);
    }

    /** Reads the named variant of a public upload. The extension is cosmetic, like on the server. */
    public DownloadedImage readVariant(String uploadKey, String transformation, String extension) {
        return readVariant(uploadKey, transformation, extension, null, null);
    }

    /** Reads the named variant, optionally with the cache version and download credential. */
    public DownloadedImage readVariant(
            String uploadKey,
            String transformation,
            String extension,
            @Nullable String version,
            @Nullable DownloadCredential credential
    ) {
        return read("/api/get/image/%s/%s.%s".formatted(uploadKey, transformation, extension), version, credential);
    }

    /** Polls the status of the key's active upload attempt. */
    public UploadStatus uploadStatus(String uploadKey) {
        return restClient.get()
                .uri("/api/upload/status/%s".formatted(uploadKey))
                .retrieve()
                .body(UploadStatus.class);
    }

    private DownloadedImage read(String path, @Nullable String version, @Nullable DownloadCredential credential) {
        var response = restClient.get()
                .uri(path + query(version, credential))
                .retrieve()
                .toEntity(byte[].class);
        var contentType = response.getHeaders().getContentType();
        var disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        return new DownloadedImage(
                contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType.toString(),
                fileNameOf(disposition),
                response.getBody());
    }

    /** Builds the {@code v}, {@code e}, {@code s}, {@code k} query string, or an empty string. */
    private static String query(@Nullable String version, @Nullable DownloadCredential credential) {
        var params = new ArrayList<String>();
        if (version != null)
            params.add("v=" + encode(version));
        if (credential != null) {
            params.add("e=" + credential.expires());
            params.add("s=" + encode(credential.signature()));
            params.add("k=" + encode(credential.keyId()));
        }
        return params.isEmpty() ? "" : "?" + String.join("&", params);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Parses the file name of a {@code inline; filename="..."} disposition header. */
    private static String fileNameOf(@Nullable String contentDisposition) {
        if (contentDisposition == null)
            return "";
        var marker = "filename=\"";
        var start = contentDisposition.indexOf(marker);
        if (start < 0)
            return "";
        var end = contentDisposition.indexOf('"', start + marker.length());
        return end < 0 ? "" : contentDisposition.substring(start + marker.length(), end);
    }

    private byte[] writeJson(Object body) {
        return jsonMapper.writeValueAsBytes(body);
    }

    /** A byte array that carries a file name for the multipart part. */
    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String fileName;

        NamedByteArrayResource(byte[] bytes, String fileName) {
            super(bytes);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}
