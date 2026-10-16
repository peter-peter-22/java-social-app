package com.example.uploads_api.variant;

import com.example.uploads_api.stored_asset.StoredAsset;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public class Variant {
    private final VariantId id;
    private final Instant createdAt;
    private final StoredAsset storedAsset;
}
