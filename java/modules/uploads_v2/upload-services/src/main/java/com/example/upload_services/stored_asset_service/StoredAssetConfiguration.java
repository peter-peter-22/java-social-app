package com.example.upload_services.stored_asset_service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "uploads.stored-asset")
public class StoredAssetConfiguration {
    /** How much retries an upload signature can use.
     * Includes retried initiated by the client and the nginx gateway.
     * (So all upload requests those reach the upload service.) */
    private final int uploadAttemptsPerSignature;
    /** How much times the media transformation can be retried.
     * This applies to both the synchronous (HTTP reset) and async (message queue reset) transformations. */
    private final int mediaTransformationRetries;
    // TODO implement usage
}
