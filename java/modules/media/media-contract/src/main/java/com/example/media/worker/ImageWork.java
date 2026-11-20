package com.example.media.worker;

import com.example.media.asset.AssetLocation;
import com.example.media.identity.AssetId;
import com.example.media.identity.AttemptId;
import com.example.media.identity.UploadKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Blocking command, and later the queue message. It carries locations and resolved operations,
 * not registry entities or transformation names to look up.
 */
@NullMarked
public record ImageWork(
        AttemptId attemptId,
        UploadKey uploadKey,
        AssetLocation source,
        @Nullable ReservedImageResult incoming,
        List<ReservedImageResult> eager
) {
    public ImageWork {
        Objects.requireNonNull(attemptId, "attemptId");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(eager, "eager");
        eager = List.copyOf(eager);

        Set<AssetId> assetIds = new HashSet<>();
        assetIds.add(source.assetId());
        if (incoming != null && !assetIds.add(incoming.assetId())) {
            throw new IllegalArgumentException("Incoming asset id collides with the source");
        }
        Set<String> names = new HashSet<>();
        for (ReservedImageResult result : eager) {
            if (!assetIds.add(result.assetId())) {
                throw new IllegalArgumentException("Each transformation must get its own asset id");
            }
            if (!names.add(result.transformationName())) {
                throw new IllegalArgumentException("Eager transformation names must be unique");
            }
        }
    }
}
