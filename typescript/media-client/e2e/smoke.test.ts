/**
 * The smoke journey of the browser client against the real stack: register a transformation,
 * upload the fixture image, read the original and the variant back, and poll the status. It runs
 * against the compose stack started by the Java end-to-end test's one command
 * (`./gradlew :media-api:integrationTest` in `../java`, with the stack left running) or by
 * `modules/media/deployment/run.bash`. Without a reachable API the test is skipped, so `npm test`
 * stays green on a machine without the stack.
 */
import { strict as assert } from "node:assert";
import { test } from "node:test";
import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { MediaClient, MediaClientError } from "../src/client.js";

const API_URL = process.env.MEDIA_API_URL ?? "http://localhost:8090";
const ADMIN_KEY = process.env.MEDIA_ADMIN_KEY ?? "dev-admin-key";

const reachable = await fetch(API_URL + "/api/upload/status/probe")
    .then(() => true)
    .catch(() => false);

test("upload and read through the browser client", { skip: !reachable }, async () => {
    const uploadKey = "smoke" + Date.now();
    const client = new MediaClient(API_URL);

    // Test setup over raw fetch: the admin operations belong to the Java admin client; the browser
    // client only uploads and reads.
    const registered = await fetch(API_URL + "/api/admin/transformation", {
        method: "POST",
        headers: { "content-type": "application/json", "X-Admin-Key": ADMIN_KEY },
        body: JSON.stringify({
            name: "smoke-profile",
            spec: {
                type: "image",
                aspectRatio: { width: 1, height: 1, mode: "FILL" },
                encoding: { type: "jpeg", quality: 80 },
            },
        }),
    });
    assert.equal(registered.status, 200);

    // The upload itself needs a permission: the admin mints one for exactly this upload, then the
    // client presents it. Only the admin knows the constraints; the upload must match them.
    const issued = await fetch(API_URL + "/api/admin/upload-permission", {
        method: "POST",
        headers: { "content-type": "application/json", "X-Admin-Key": ADMIN_KEY },
        body: JSON.stringify({
            uploadKey,
            extension: "jpg",
            eagerTransformations: ["smoke-profile"],
            visibility: "PUBLIC",
            maxUses: 1,
        }),
    });
    assert.equal(issued.status, 200);
    const { permission } = (await issued.json()) as { permission: string };

    const fixture = new Uint8Array(
        await readFile(fileURLToPath(new URL("../../e2e/fixtures/image.jpg", import.meta.url))));
    const result = await client.uploadImage(
        {
            permission,
            uploadKey,
            extension: "jpg",
            eagerTransformations: ["smoke-profile"],
            async: false,
        },
        fixture,
        "image.jpg",
        "image/jpeg",
    );

    assert.equal(result.uploadKey, uploadKey);
    assert.equal(result.async, false);

    const original = await client.readOriginal(uploadKey, "jpg");
    assert.equal(original.contentType, "image/jpeg");
    assert.ok(original.content.length > 0);

    const variant = await client.readVariant(uploadKey, "smoke-profile", "jpg");
    assert.equal(variant.contentType, "image/jpeg");
    assert.ok(variant.content.length > 0);

    const status = await client.uploadStatus(uploadKey);
    assert.equal(status.status, "SUCCESS");
});

test("private upload and read through the browser client", { skip: !reachable }, async () => {
    const uploadKey = "smoke-private" + Date.now();
    const client = new MediaClient(API_URL);

    // Test setup over raw fetch: the admin operations belong to the Java admin client.
    const registered = await fetch(API_URL + "/api/admin/transformation", {
        method: "POST",
        headers: { "content-type": "application/json", "X-Admin-Key": ADMIN_KEY },
        body: JSON.stringify({
            name: "smoke-private-profile",
            spec: {
                type: "image",
                aspectRatio: { width: 1, height: 1, mode: "FILL" },
                encoding: { type: "jpeg", quality: 80 },
            },
        }),
    });
    assert.equal(registered.status, 200);

    const issued = await fetch(API_URL + "/api/admin/upload-permission", {
        method: "POST",
        headers: { "content-type": "application/json", "X-Admin-Key": ADMIN_KEY },
        body: JSON.stringify({
            uploadKey,
            extension: "jpg",
            eagerTransformations: ["smoke-private-profile"],
            visibility: "PRIVATE",
            maxUses: 1,
        }),
    });
    assert.equal(issued.status, 200);
    const { permission } = (await issued.json()) as { permission: string };

    const fixture = new Uint8Array(
        await readFile(fileURLToPath(new URL("../../e2e/fixtures/image.jpg", import.meta.url))));
    const result = await client.uploadImage(
        { permission, uploadKey, extension: "jpg", eagerTransformations: ["smoke-private-profile"], async: false },
        fixture,
        "image.jpg",
        "image/jpeg",
    );
    assert.equal(result.uploadKey, uploadKey);

    // Without a download token the private read is closed.
    await assert.rejects(
        client.readOriginal(uploadKey, "jpg"),
        (error: unknown) => error instanceof MediaClientError && error.status === 403,
    );

    // Mint a download token for the upload's version and read the original and variant with it.
    const download = await fetch(API_URL + "/api/admin/download-permission", {
        method: "POST",
        headers: { "content-type": "application/json", "X-Admin-Key": ADMIN_KEY },
        body: JSON.stringify({ uploadKey }),
    });
    assert.equal(download.status, 200);
    const token = (await download.json()) as {
        version: string;
        expires: number;
        signature: string;
        keyId: string;
    };
    const credential = { expires: token.expires, signature: token.signature, keyId: token.keyId };

    const original = await client.readOriginal(uploadKey, "jpg", token.version, credential);
    assert.ok(original.content.length > 0);

    const variant = await client.readVariant(uploadKey, "smoke-private-profile", "jpg", token.version, credential);
    assert.ok(variant.content.length > 0);
});
