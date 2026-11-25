/**
 * The browser client of the media service web API: uploads images, reads originals and variants,
 * and polls the status of queued uploads. Mirrors the Java `media-client` module; the wire shape is
 * the shared media JSON shape of the server.
 *
 * Errors are thrown as `MediaClientError` carrying the response status. The `fetch` implementation
 * is injectable for tests; the default is the global `fetch` (browsers and Node 18+).
 */
import type {
    UploadImageRequest,
    UploadResult,
    UploadStatus,
    DownloadCredential,
} from "./types.js";

export type {
    ImageEncoding,
    AspectRatio,
    LimitResolution,
    ImageTransformationSpec,
    UploadImageRequest,
    UploadResult,
    UploadAttemptStatus,
    UploadStatus,
    DownloadCredential,
} from "./types.js";

/** A downloaded file: the content type and the stored bytes. */
export interface DownloadedImage {
    contentType: string;
    fileName: string;
    content: Uint8Array;
}

/** An error answer of the media service. */
export class MediaClientError extends Error {
    readonly status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = "MediaClientError";
        this.status = status;
    }
}

export class MediaClient {
    private readonly baseUrl: string;
    private readonly fetchImpl: typeof fetch;

    constructor(baseUrl: string, fetchImpl: typeof fetch = fetch) {
        this.baseUrl = baseUrl.replace(/\/+$/, "");
        this.fetchImpl = fetchImpl;
    }

    /**
     * Uploads an image file. A blocking upload (async false or absent) is finalized when the
     * promise resolves; a queued upload (async true) must be polled with `uploadStatus`.
     */
    async uploadImage(
        request: UploadImageRequest,
        file: Uint8Array<ArrayBuffer>,
        fileName: string,
        contentType: string,
    ): Promise<UploadResult> {
        const form = new FormData();
        form.append(
            "body",
            new Blob([JSON.stringify(request)], { type: "application/json" }),
        );
        form.append("file", new Blob([file], { type: contentType }), fileName);

        const response = await this.fetchImpl(this.baseUrl + "/api/upload/image", {
            method: "POST",
            body: form,
        });
        await ensureOk(response);
        return (await response.json()) as UploadResult;
    }

    /**
     * Reads the original file, optionally with the cache version and download credential a private
     * upload requires. A public read ignores them; `version` is then only a cache key.
     */
    readOriginal(
        uploadKey: string,
        extension: string,
        version?: string,
        credential?: DownloadCredential,
    ): Promise<DownloadedImage> {
        return this.read(`/api/get/image/${uploadKey}.${extension}`, version, credential);
    }

    /** Reads the named variant, optionally with the cache version and download credential. */
    readVariant(
        uploadKey: string,
        transformation: string,
        extension: string,
        version?: string,
        credential?: DownloadCredential,
    ): Promise<DownloadedImage> {
        return this.read(`/api/get/image/${uploadKey}/${transformation}.${extension}`, version, credential);
    }

    /** Polls the status of the key's active upload attempt. */
    async uploadStatus(uploadKey: string): Promise<UploadStatus> {
        const response = await this.fetchImpl(this.baseUrl + "/api/upload/status/" + uploadKey);
        await ensureOk(response);
        return (await response.json()) as UploadStatus;
    }

    private async read(
        path: string,
        version?: string,
        credential?: DownloadCredential,
    ): Promise<DownloadedImage> {
        const response = await this.fetchImpl(this.baseUrl + path + query(version, credential));
        await ensureOk(response);
        return {
            contentType: response.headers.get("content-type") ?? "application/octet-stream",
            fileName: fileNameOf(response.headers.get("content-disposition")),
            content: new Uint8Array(await response.arrayBuffer()),
        };
    }
}

async function ensureOk(response: Response): Promise<void> {
    if (!response.ok)
        throw new MediaClientError(response.status, `the media service answered ${response.status}`);
}

/** Parses the file name of a `inline; filename="..."` disposition header. */
function fileNameOf(contentDisposition: string | null): string {
    if (contentDisposition === null)
        return "";
    const marker = 'filename="';
    const start = contentDisposition.indexOf(marker);
    if (start < 0)
        return "";
    const end = contentDisposition.indexOf('"', start + marker.length);
    return end < 0 ? "" : contentDisposition.substring(start + marker.length, end);
}

/** Builds the `v`, `e`, `s`, `k` query string for a read URL, or an empty string. */
function query(version?: string, credential?: DownloadCredential): string {
    const params = new URLSearchParams();
    if (version !== undefined)
        params.set("v", version);
    if (credential !== undefined) {
        params.set("e", String(credential.expires));
        params.set("s", credential.signature);
        params.set("k", credential.keyId);
    }
    const encoded = params.toString();
    return encoded ? `?${encoded}` : "";
}
