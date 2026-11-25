/**
 * The wire types of the media service: the exact JSON shape defined by the `media-json` module of
 * the Java build (the polymorphic `type` properties are the versioning seam there, so they are
 * fixed here too). If the Java shape tests change, these types must follow.
 */

/** The output encoding of an image transformation. */
export type ImageEncoding =
    | { type: "jpeg"; quality: number }
    | { type: "webp"; quality: number };

/** Crops or pads the image to the requested aspect ratio. */
export interface AspectRatio {
    width: number;
    height: number;
    mode: "FILL" | "CONTAIN";
}

/** Limits the resolution on one axis. */
export interface LimitResolution {
    pixels: number;
    mode: "KEEP_ASPECT_RATIO";
}

/** The resolved operations of an image transformation. */
export interface ImageTransformationSpec {
    type: "image";
    limitWidth?: LimitResolution | null;
    limitHeight?: LimitResolution | null;
    aspectRatio?: AspectRatio | null;
    encoding: ImageEncoding;
}

/** The JSON body part of an image upload request. */
export interface UploadImageRequest {
    uploadKey?: string;
    extension?: string;
    desiredContentType?: string;
    desiredBytes?: number;
    eagerTransformations?: string[];
    incomingTransformation?: string;
    /** Queues the eager and incoming transformations; the upload is not readable until they finish. */
    async?: boolean;
}

/** The accepted upload; `async` is true when the transformations are queued. */
export interface UploadResult {
    uploadKey: string;
    createdAt: string;
    async: boolean;
}

/** The state of an upload attempt. */
export type UploadAttemptStatus = "PENDING" | "SUCCESS" | "FAILED";

/** The status of the key's active upload attempt. */
export interface UploadStatus {
    uploadKey: string;
    status: UploadAttemptStatus;
}
