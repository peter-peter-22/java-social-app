package com.example.media.api.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * The upload permission settings ({@code media.upload-permission}): whether an upload must present a
 * permission, and the issuance limits.
 *
 * <p>{@link #required()} is the enforcement switch: when it is true, a start-image-upload request
 * without a permission is rejected as denied (403). It defaults to true, so a deployment that
 * configures nothing fails closed.</p>
 *
 * <p>The issuance clamps a requested TTL and use budget to {@link #maxTtl()} and {@link #maxUses()}.
 * The defaults are the values used when the caller leaves a value out; they are code defaults, so a
 * deployment that overrides nothing still has a bounded budget. The deployment stack overrides them
 * through the environment.</p>
 */
@ConfigurationProperties(prefix = "media.upload-permission")
@NullMarked
public record UploadPermissionProperties(
        @DefaultValue("true") boolean required,
        @DefaultValue("10m") Duration defaultTtl,
        @DefaultValue("1h") Duration maxTtl,
        @DefaultValue("1") int defaultMaxUses,
        @DefaultValue("10") int maxUses
) {
    public UploadPermissionProperties {
        if (defaultTtl.isZero() || defaultTtl.isNegative())
            throw new IllegalArgumentException("default-ttl must be positive: " + defaultTtl);
        if (maxTtl.compareTo(defaultTtl) < 0)
            throw new IllegalArgumentException("max-ttl must not be shorter than default-ttl: " + maxTtl);
        if (defaultMaxUses <= 0)
            throw new IllegalArgumentException("default-max-uses must be positive: " + defaultMaxUses);
        if (maxUses < defaultMaxUses)
            throw new IllegalArgumentException("max-uses must not be smaller than default-max-uses: " + maxUses);
    }
}
