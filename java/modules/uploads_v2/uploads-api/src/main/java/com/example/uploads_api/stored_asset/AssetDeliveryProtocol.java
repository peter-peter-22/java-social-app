package com.example.uploads_api.stored_asset;

import com.example.uploads_api.uploads.UploadType;
import org.jspecify.annotations.NullMarked;

/**
 * The delivery protocol tells the client how to access and handle an asset.
 * E.g., an image and a video requires different display elements in the browser.
 */
@NullMarked
public enum AssetDeliveryProtocol {
    IMAGE,
    VIDEO,
    HLS_CMAF,
    RAW;

    public static AssetDeliveryProtocol uploadTypeToDeliveryProtocol(UploadType uploadType) {
        return switch (uploadType) {
            case IMAGE -> AssetDeliveryProtocol.IMAGE;
            case VIDEO -> AssetDeliveryProtocol.VIDEO;
            case RAW -> AssetDeliveryProtocol.RAW;
        };
    }
}
