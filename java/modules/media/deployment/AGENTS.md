# Compose deployment for the media service
This directory contains a docker compose file and some bash scripts for starting the image worker, the nginx edge, the database, and the object storage.

Starting the media API in addition makes the whole network work together, making the end-to-end test runnable.

## Usage
- Build the docker images the stack runs — the JDK + libvips/ffmpeg worker image (`media.Dockerfile`) and the nginx edge (`nginx.Dockerfile`) — with `./build.bash` (the end-to-end test and the transformation test scripts build them automatically).
- Build the media API and image worker jars and start the compose file with the `run.bash` in this directory.
- Start the `media-api` application or run the end-to-end integration test in it (`EndToEndTest` in `media-api`).

## Tests
- `./gradlew :media-api:integrationTest` (from the Gradle root) runs the end-to-end test: it builds the worker jar and this directory's dev images, boots this compose file with Testcontainers, and drives the API through the `media-client` module. The CDN journeys address the edge directly with a plain `RestClient`: the client speaks the web API, and `/cdn/` is the edge's URL space.
- `./test-transformations.bash` runs the image transformation output tests in the dev image (the tests need the native libvips library).
- `./test-video-transformations.bash` runs the video transformation output tests in the dev image (the tests need the ffmpeg/ffprobe binaries).
- The compose file must not set `container_name:` — Testcontainers rejects fixed names.

## The CDN edge and the download tokens

The `cdn` service is the minimal edge verifier of private reads on the fixed host port `8083`; it is not a cache. `nginx.conf` declares `/cdn/`, and `download_token.js` is its njs `js_access` handler.

- The API stays authoritative. The edge proves that the presented signature is the HMAC of this upload key, version, and expiry, and that the token has not expired. It cannot know the current version of an upload (that needs the database), so a correctly signed token for an old version passes the edge and the API rejects it with 403. The visibility is enforced by the API only.
- A read without any token parameter passes through: the API serves a public upload and rejects a private one with 403. A partial token (some but not all of `v`, `e`, `s`, `k`) is always rejected at the edge with 403, so the edge and the API cannot disagree about what makes up a token.
- The signature is base64url HMAC-SHA256 (no padding) over `"m1\nread\n" + keyId + "\n" + uploadKey + "\n" + version + "\n" + expires`. The upload key is the first path segment after `/cdn/`, with the extension or the variant path removed; it is restricted to `[A-Za-z0-9_-]{1,100}`, so the segment is unambiguous. The version is `upload.createdAt.toEpochMilli()` as a decimal string, so every read URL of one upload version shares one cache entry and a replaced upload produces new URLs.
- URL shapes: `/cdn/{uploadKey}.{extension}` and `/cdn/{uploadKey}/{transformation}.{extension}`, with the query `?v={version}&e={expires}&s={signature}&k={keyId}`. The edge strips `/cdn/` and proxies to the API's `/api/get/image/` paths, passing `v`, `e`, `s`, and `k` through unchanged.
- Key rotation is an overlap: the API signs with its current key, and `MEDIA_DOWNLOAD_TOKEN_PREVIOUS_KEY_ID`/`_SECRET` still verify tokens minted before the switch. Set both or neither; an unknown `k` is rejected. Rotate by moving the current pair to previous, setting new current values, and dropping the previous pair once the old tokens expired.
- Cache headers come from the API: `public, max-age=31536000, immutable` for public reads, `private, max-age=300` for private reads, and `Cache-Control: no-store` only on the permission/token issuance responses. Private media stays cacheable and the edge has no cache, so browser caching works and no cached private response can leak. The edge and the API also answer `Referrer-Policy: no-referrer`.
- Secrets: `MEDIA_DOWNLOAD_TOKEN_SECRET` (base64; its decoded bytes must be at least 32 and are the HMAC key), `MEDIA_DOWNLOAD_TOKEN_KEY_ID`, and the optional previous pair must be the same values the API signs with. `MEDIA_DOWNLOAD_TOKEN_CLOCK_SKEW_SECONDS` is the API's `media.download-token.clock-skew` expressed in seconds; both verifiers must allow the same window. `MEDIA_API_BASE_URL` is the API the edge proxies to (the host gateway, like the worker's completion callback). The defaults serve local development; production must inject them through the environment.
- Log redaction: the access log format records `$uri`, which has no query string, so `e`, `s`, and `k` never reach the logs. Never add a format that logs `$request` or `$args`, and keep token-bearing URLs out of error and debug logs.
- `js_access` exists since njs 0.9.9; the image pins `nginx:1.31-alpine` (njs 1.0.2, which also carries the CVE fix in `js_access`). `nginx.conf` is an envsubst template: the image substitutes only the `MEDIA_*` variables and writes the result to `/etc/nginx/nginx.conf`.

## Deferred: edge caching and the direct-Minio CDN

This edge verifies and proxies; it does not cache and does not read Minio. Both steps are deferred and
documented in `../AGENTS.md` (Future work). If edge caching is added, authorization must run in the
access phase before the cache lookup (`auth_request` to an API endpoint that answers 204/403), the
cache key must be `$uri$arg_v` without `e`, `s`, or `k`, and a token-less private read must never be
served from the cache.