package com.example.uploads_api.stored_asset;

import com.example.uploads_api.uploads.UploadType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum AssetDeliveryProtocol {
    IMAGE,
    VIDEO,
    HLS_CMAF,
    RAW;

    /** Get the delivery protocol for the uploaded (original) file based on the upload type. */
    public static AssetDeliveryProtocol uploadTypeToDeliveryProtocol(UploadType uploadType) {
        return switch (uploadType) {
            case IMAGE -> AssetDeliveryProtocol.IMAGE;
            case VIDEO -> AssetDeliveryProtocol.VIDEO;
            case RAW -> AssetDeliveryProtocol.RAW;
        };
    }
}
