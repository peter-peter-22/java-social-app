/**
 * The client unit tests: the requests the client sends and the responses it parses, against a fake
 * fetch. The wire shapes are the media JSON shapes; the smoke journey against the real stack lives
 * in `e2e/smoke.test.ts`.
 */
import { strict as assert } from "node:assert";
import { test } from "node:test";
import { MediaClient, MediaClientError } from "../src/client.js";

interface CapturedRequest {
    url: string;
    method: string;
    body: FormData | null;
    contentType: string | null;
}

/** A fake fetch that records the request and answers with the given response. */
function fakeFetch(
    response: Response,
): { fetchImpl: typeof fetch; captured: CapturedRequest[] } {
    const captured: CapturedRequest[] = [];
    const fetchImpl: typeof fetch = async (input, init) => {
        const request = new Request(input, init);
        captured.push({
            url: request.url,
            method: request.method,
            body: request.body === null ? null : await request.formData(),
            contentType: request.headers.get("content-type"),
        });
        return response.clone();
    };
    return { fetchImpl, captured };
}

function jsonResponse(body: unknown, status = 200): Response {
    return new Response(JSON.stringify(body), {
        status,
        headers: { "content-type": "application/json" },
    });
}

test("uploads an image as multipart with a JSON body part", async () => {
    const { fetchImpl, captured } = fakeFetch(jsonResponse(
        { uploadKey: "cover", createdAt: "2026-01-01T00:00:00Z", async: true },
    ));
    const client = new MediaClient("http://api.test/", fetchImpl);

    const result = await client.uploadImage(
        { uploadKey: "cover", extension: "jpg", eagerTransformations: ["profile"], async: true },
        new Uint8Array([1, 2, 3]),
        "image.jpg",
        "image/jpeg",
    );

    assert.equal(captured.length, 1);
    assert.equal(captured[0].url, "http://api.test/api/upload/image");
    assert.equal(captured[0].method, "POST");
    assert.ok(captured[0].contentType?.startsWith("multipart/form-data"));
    const bodyPart = captured[0].body?.get("body") as Blob;
    const bodyJson = JSON.parse(await bodyPart.text());
    assert.equal(bodyJson.uploadKey, "cover");
    assert.equal(bodyJson.async, true);
    assert.deepEqual(bodyJson.eagerTransformations, ["profile"]);
    const filePart = captured[0].body?.get("file") as File;
    assert.equal(filePart.name, "image.jpg");
    assert.equal(filePart.type, "image/jpeg");
    assert.equal(result.uploadKey, "cover");
    assert.equal(result.createdAt, "2026-01-01T00:00:00Z");
    assert.equal(result.async, true);
});

test("reads the original with its content type and file name", async () => {
    const { fetchImpl, captured } = fakeFetch(new Response(new Uint8Array([9, 9]), {
        status: 200,
        headers: {
            "content-type": "image/webp",
            "content-disposition": 'inline; filename="cover.jpg"',
        },
    }));
    const client = new MediaClient("http://api.test", fetchImpl);

    const image = await client.readOriginal("cover", "jpg");

    assert.equal(captured[0].url, "http://api.test/api/get/image/cover.jpg");
    assert.equal(image.contentType, "image/webp");
    assert.equal(image.fileName, "cover.jpg");
    assert.deepEqual(image.content, new Uint8Array([9, 9]));
});

test("reads the variant under its transformation path", async () => {
    const { fetchImpl, captured } = fakeFetch(new Response(new Uint8Array([7]), {
        status: 200,
        headers: { "content-type": "image/jpeg" },
    }));
    const client = new MediaClient("http://api.test", fetchImpl);

    const image = await client.readVariant("cover", "profile", "jpg");

    assert.equal(captured[0].url, "http://api.test/api/get/image/cover/profile.jpg");
    assert.equal(image.contentType, "image/jpeg");
});

test("reads the upload status", async () => {
    const { fetchImpl, captured } = fakeFetch(jsonResponse({ uploadKey: "cover", status: "PENDING" }));
    const client = new MediaClient("http://api.test", fetchImpl);

    const status = await client.uploadStatus("cover");

    assert.equal(captured[0].url, "http://api.test/api/upload/status/cover");
    assert.equal(status.uploadKey, "cover");
    assert.equal(status.status, "PENDING");
});

test("an error answer is thrown with its status", async () => {
    const { fetchImpl } = fakeFetch(new Response("", { status: 404 }));
    const client = new MediaClient("http://api.test", fetchImpl);

    await assert.rejects(
        client.readOriginal("missing", "jpg"),
        (error: unknown) => error instanceof MediaClientError && error.status === 404,
    );
});
