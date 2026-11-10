package com.example.upload_registry.stored_asset_service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "uploads.stored-asset")
public class StoredAssetConfiguration {
    /** The upload of an asset is considered failed after this time.
     * The ongoing object upload is canceled if still ongoing.
     * This does not limit the duration of the whole upload process, it applies to a single asset upload.
     * (There are typically at least 2 asset uploads after each other: browser->original, media worker->variant.) */
    private final Duration pendingUploadExpiration;
    /** How much must the cleanup wait before the deletion of an expired asset.
     * This is necessary because the ongoing upload intends to stop at the moment the expiration is reached, and it
     * needs additional time to completely stop. */
    private final Duration pendingUploadShutdownTime;
    /** How much retries an upload signature can use between the client and the API gateway.
     * This means the retries initiated by the API gateway won't count.
     * Only the attempts with unique request ID count.
     * (The request IDs are generated once a request reached the gateway, and they stay the same for the gateway initiated retries.)
     * The goal of this it to prevent the repeated abuse of the same upload signature. */
    // TODO implement nginx request id and spring unique retry counter
    private final int uploadAttemptsPerSignature;
    /** How much times the media transformation can be retried.
     * This applies to both the synchronous (HTTP reset) and async (message queue reset) transformations. */
    private final int mediaTransformationRetries;
}
