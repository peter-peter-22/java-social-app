package com.example.uploads_api.stored_asset;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public final class StoredAsset{
    private final UUID id;
    private final String homeRegion;
    private final boolean directory;
    @Nullable private final String entryPoint; // TODO make this not nullable
    private final String mimeType;
    @Nullable private final String extension;
    private final String contentType; // TODO add the content type to the underlying entity repository and SQL
    private final Instant createdAt;
    private final long bytes;
    private final String deliveryProtocol;
    private final StoredAssetStatus status;
    /** After this date, the ongoing uploads stop, and the asset is awaiting deletion.
     * Used for removing failed uploads reliably.
     * An upload that is not linked */
    private final Instant expirationDate;
    @Nullable private final ImageMetadata imageMetadata;
    @Nullable private final VideoMetadata videoMetadata;

    // TODO should the stored asset directory get a list of it's files

    /** The contents of a stored asset are relative to this object key prefix. */
    public String getDirectoryPrefix(){
        return id.toString();
    }

    /** The object key of the entrypoint.
     * If this is not a directory, it returns the path of the only object.
     * Otherwise, it returns the entrypoint file of the directory (typically the HLS master file). */
    public String getMainObjectKey(){
        return getDirectoryPrefix()+"/entryPoint."+extension;
    }
}
