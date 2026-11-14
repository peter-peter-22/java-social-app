package com.example.upload_registry.stored_asset_service;

import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public class CreateStoredAsset {
    private final boolean directory;
    @Nullable private final String entryPoint;
    private final String mimeType;
   @Nullable private final String extension;
    private final long bytes;
    private final Collection<String> objects;
    // TODO add the delivery protocol field to the entity and SQL table
    private final AssetDeliveryProtocol deliveryProtocol;
}
