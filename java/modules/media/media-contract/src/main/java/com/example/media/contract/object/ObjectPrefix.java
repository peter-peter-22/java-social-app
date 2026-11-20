package com.example.media.contract.object;

import java.util.Objects;

/**
 * The reserved prefix that owns every object of one asset.
 * The asset id is the prefix, so the keys of two assets cannot collide.
 *
 * <p>Key policy: the key of a stored file is {@code keyPrefix + "/" + relativePath} with every
 * {@code '.'} replaced by {@code '_'}. All key derivation goes through {@link #resolve}, and a
 * worker only writes under the prefixes its command carries, so the policy cannot drift between
 * the API and the workers.</p>
 */
public record ObjectPrefix(String region, String bucket, String keyPrefix) {
    public ObjectPrefix {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucket, "bucket");
        Objects.requireNonNull(keyPrefix, "keyPrefix");
    }

    /**
     * The object that holds the file stored at {@code relativePath} under this prefix.
     * The '.' character is replaced because the object store key rules forbid it.
     */
    public ObjectRef resolve(String relativePath) {
        Objects.requireNonNull(relativePath, "relativePath");
        return new ObjectRef(region, bucket, keyPrefix + "/" + relativePath.replace('.', '_'));
    }
}