package com.example.media.contract.asset;

/**
 * The delivery protocol tells the client how to access and handle an asset.
 * E.g., an image and a video require different display elements in the browser.
 * The names are the values stored in the registry's delivery_protocol column; do not rename them.
 */
public enum AssetDeliveryProtocol {
    IMAGE,
    VIDEO,
    HLS_CMAF,
    RAW
}