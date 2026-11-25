# Media upload and download authorization

**Status:** Proposed  
**Scope:** media service (`java/modules/media`), Java and TypeScript clients, deployment stack  
**Related:** `modules/media/AGENTS.md`, `planning/uploads/overview.md`, `planning/uploads/upload-lifecycle.md`

## 1. Goal

Add two independent authorization mechanisms to the media service:

1. **Upload permission**
   - Issued by the server that controls the media admin client (the future main application).
   - Required before a client upload.
   - Bound to the upload key, file constraints, transformations, and visibility.
   - Has expiration and a usage limit.
   - Must be secure.

2. **Download permission**
   - Issued when a client views a private upload.
   - Grants read access to all variants and files under one upload key.
   - Has expiration only.
   - Must be verifiable by the planned nginx CDN.
   - Security is not critical; a self-contained signed token is sufficient.

Also add:

- A `PUBLIC` / `PRIVATE` visibility on uploads.
- Versioned read URLs so browser and CDN caches are invalidated when an upload is replaced.
- A minimal nginx edge verifier in the deployment stack. The full direct-Minio CDN is deferred.

### Non-goals

- User-level authorization. The main application decides whether a user may upload or view an item; the media service only enforces the upload key and constraints.
- Replacing the static admin key with signed admin requests or mTLS. This remains future hardening.
- Immediate revocation of download tokens. Download tokens are valid until expiry unless an `auth_request` check is added later.
- Full nginx CDN caching, direct Minio proxying, and regional routing.
- On-demand transformations.
- Periodic cleanup of expired permission rows.

## 2. Decisions

| Topic | Decision | Reason |
|---|---|---|
| Upload permission storage | Database-backed opaque bearer ticket | Usage limits and revocation require server-side state. The media API already has a database. |
| Upload token format | `up1_` + 32 random bytes, base64url | High entropy, easy to distinguish from download tokens. Store only the SHA-256 hash. |
| Download permission | Stateless HMAC-SHA256 signed token | Expiry-only and edge-verifiable without a database lookup. |
| Download signing algorithm | HMAC-SHA256 with key id | Simple, nginx-friendly, and sufficient because download security is not critical. RSA/Ed25519 is optional if the edge must hold only a public key. |
| Nginx timing | Build a minimal verifier now; full CDN later | The token contract must be tested at the edge. A Java-only implementation can drift from nginx behavior. |
| Visibility | Stored on the finalized upload and on the attempt | Async completion reloads the attempt and must know the visibility. Existing uploads default to `PUBLIC`. |
| Read cache version | `upload.createdAt.toEpochMilli()` as a decimal string | Already available on `Upload`; no extra column. Changes when the upload is replaced. |
| Version in private token | Include the version in the signed payload | Prevents an old token from reading a replaced upload at the same key. |
| Private media caching | Cacheable. Do **not** disable cache control. | Users can save files anyway; disabling caching adds little security and hurts performance. |
| Credential response caching | `Cache-Control: no-store` on permission/token issuance responses | Those responses contain the credentials themselves. |
| Upload key charset | Restrict to `[A-Za-z0-9_-]{1,100}` | Required for a clean URL prefix, nginx path matching, and unambiguous HMAC input. |
| Failed upload attempt | Revoke the permission’s remaining uses by default | Matches the existing `UploadAttemptStatus.FAILED` TODO. A retry then needs a new permission. |

## 3. Current state

- `/api/admin/**` is guarded by `AdminKeyFilter` with the static `media.admin-key` / `X-Admin-Key`.
- Uploads have no permission field.
- Reads are effectively public; `ReadOriginal` and `ReadVariant` do not check visibility.
- Object keys are asset-id prefixes, not upload-key prefixes. The upload key is only a database key.
- `Upload` already has `createdAt`, so the cache version does not need a schema change.
- The end-to-end test already uses `MediaAdminClient` and `MediaClient`, so the test can act as the future main application.

## 4. Contracts

### 4.1 Upload permission

#### Token

```
up1_<base64url(32 random bytes)>
```

- Store `SHA-256(full token)` as hex, never the raw token.
- Return the raw token exactly once from the admin endpoint.
- SQL stores `token_hash varchar(64)` with a unique index.

#### Admin endpoint

```
POST /api/admin/upload-permission
X-Admin-Key: ...
Content-Type: application/json

{
  "uploadKey": "cover_123",          // optional; server generates a safe key if absent
  "extension": "jpg",
  "contentType": "image/jpeg",
  "maxBytes": 10485760,
  "eagerTransformations": ["profile", "thumb"],
  "incomingTransformation": "optimize",
  "visibility": "PRIVATE",
  "expiresInSeconds": 600,
  "maxUses": 1
}
```

Response:

```json
{
  "permissionId": "…",
  "uploadKey": "cover_123",
  "permission": "up1_…",
  "expiresAt": "2026-11-25T12:00:00Z",
  "maxUses": 1
}
```

Response headers:

```
Cache-Control: no-store
```

#### Upload request

`ImageUploadRequest` gains a `permission` field:

```json
{
  "permission": "up1_…",
  "uploadKey": "cover_123",
  "extension": "jpg",
  "eagerTransformations": ["profile"],
  "incomingTransformation": "optimize",
  "async": false
}
```

When a permission is present, its stored values are authoritative. The request values must match or the upload is rejected with 403.

#### Validation flow

Inside the existing `StartImageUpload` transaction:

1. Require `permission` when `media.upload-permission.required=true`.
2. Hash the token and lock the `upload_permission` row by `token_hash`.
3. Check `status = ACTIVE`, `expires_at > now`, and `uses < max_uses`.
4. Compare the request with the stored constraints:
   - upload key,
   - extension,
   - content type,
   - actual file size against `max_bytes`,
   - eager transformation names including order,
   - incoming transformation name.
5. Atomically increment `uses`; set `EXHAUSTED` when the last use is consumed.
6. Use the permission’s upload key and visibility.
7. Create the process and attempt in the same transaction, recording `upload_permission_id` and `visibility`.

If the transaction rolls back, the reservation rolls back too.

#### Usage semantics

- `uses` counts upload attempts, not successful uploads.
- A request rejected before consumption does not consume a use.
- A request that passes validation but fails later in the object write or worker consumes a use. This bounds abuse.
- `maxUses = 1` is the strict default.
- `maxUses > 1` permits retries but allows concurrent attempts for the same key. The existing one-active-attempt rule still protects the final upload.
- On terminal `FAILED`, revoke remaining uses by default.

### 4.2 Download permission

#### Token

```
canonical = "m1\nread\n" + keyId + "\n" + uploadKey + "\n" + version + "\n" + expiresEpochSeconds
signature = base64url(HMAC-SHA256(secret[keyId], canonical))
```

Read URL:

```
/api/get/image/{uploadKey}.{extension}?v={version}&e={expires}&s={signature}&k={keyId}
/api/get/image/{uploadKey}/{transformation}.{extension}?v={version}&e={expires}&s={signature}&k={keyId}
```

CDN URL:

```
/cdn/{uploadKey}.{extension}?v={version}&e={expires}&s={signature}&k={keyId}
/cdn/{uploadKey}/{transformation}.{extension}?v={version}&e={expires}&s={signature}&k={keyId}
```

- The signature covers the upload key **prefix**, the version, and the expiry. It grants all variants and files of that upload version.
- `keyId` supports secret rotation. If rotation is not needed yet, keep the field in the format and use a single key id.
- `version` is `upload.createdAt.toEpochMilli()` rendered as a decimal string.
- The token is not encrypted. The claims are public; the signature prevents tampering.
- Do not use unauthenticated encryption. If encryption is required, use AES-GCM, but HMAC is simpler for nginx.

#### Admin endpoint

```
POST /api/admin/download-permission
X-Admin-Key: ...
Content-Type: application/json

{ "uploadKey": "cover_123", "expiresInSeconds": 900 }
```

Response:

```json
{
  "uploadKey": "cover_123",
  "version": "1790000000123",
  "expiresAt": "2026-11-25T12:15:00Z",
  "expires": 1790000000,
  "signature": "…",
  "keyId": "k1",
  "pathPrefix": "/cdn/cover_123"
}
```

Response headers:

```
Cache-Control: no-store
```

The endpoint verifies that the upload exists and is finalized. It may also verify that the upload is `PRIVATE`; issuing for a public upload is harmless.

#### Validation flow

`ReadOriginal` and `ReadVariant` gain:

```java
read(UploadKey key, @Nullable String version, @Nullable DownloadCredential credential)
```

1. Load the upload.
2. If `visibility = PUBLIC`, serve normally. `v` is optional and only a cache key.
3. If `visibility = PRIVATE`:
   - require `v`, `e`, `s`, and `k`;
   - validate the HMAC over `keyId`, upload key, version, and expiry;
   - verify that `v` equals the current `upload.createdAt` version;
   - otherwise return 403.
4. Serve the stored file.

Status codes:

| Case | Status |
|---|---|
| Private upload, missing/invalid/expired token | 403 |
| Private upload, token for another key or version | 403 |
| Unknown upload | 404 |
| Missing/wrong admin key | 401 |
| Bad admin request | 400 |

### 4.3 Versioned read URLs and cache behavior

The version is derived from the upload creation time:

```java
String version = Long.toString(upload.createdAt().toEpochMilli());
```

Expose it to clients:

- `ImageUploadResponse` gains `@Nullable String version`.
  - Blocking upload: set to the finalized upload’s version.
  - Async accepted response: `null`, because the upload does not exist yet.
- `UploadStatusResponse` gains `@Nullable String version`.
  - Set only when `status = SUCCESS`, by loading the finalized upload.
- `MediaClient.UploadResult` and `MediaClient.UploadStatus` mirror the fields.
- TypeScript `UploadResult` and `UploadStatus` mirror the fields.

Read URL behavior:

- Public upload: `v` is optional. It is used only as a cache key.
- Private upload: `v` is required and must match the signed version.
- When an upload is replaced, its new `createdAt` produces a new `v`; browser and CDN caches miss and fetch the new content.

#### Cache headers

Media responses remain cacheable:

| Response | Cache-Control |
|---|---|
| Public read | `public, max-age=31536000, immutable` (configurable) |
| Private read | `private, max-age=300` by default (configurable) |
| Permission/token issuance | `no-store` |

Notes:

- Do **not** set `no-store` on private media responses.
- `private, max-age=...` permits browser caching. If the controlled CDN is the only shared cache and authorization runs before cache lookup, the setting can be changed to `public, max-age=...`.
- `immutable` is safe because the URL contains the version. A replaced upload produces a new URL.
- Token and permission responses must remain `no-store`.

#### Nginx cache safety

Private media is cacheable, so the edge must never serve a cached private response before authorization.

- Phase 5 (minimal nginx): proxy to the API and leave edge caching disabled. Browser caching still works through the response headers.
- Phase 6 (optional edge cache): use `auth_request` to the media API, or an always-token private location, before `proxy_cache`.
- Cache key: `$scheme$request_method$host$uri$arg_v`.
  - Include `v`.
  - Exclude `e`, `s`, and `k` so all valid tokens for the same version share one cache entry.
- Authorization must run in the access phase before cache lookup. A request without a valid token must receive 403, not a cached private response.
- Mask `e`/`s`/`k` in access logs and set `Referrer-Policy: no-referrer`.

## 5. Data model changes

Add one new Liquibase folder, for example `db/changelog/permissions/`, included after `upload_process` and before `variant` in `db.changelog-master.yaml`.

```sql
-- 000-create-upload-permission-table.sql

create table upload_permission (
    id uuid primary key default gen_random_uuid(),
    token_hash varchar(64) not null unique,
    upload_key varchar(100) not null,
    extension varchar(20) not null,
    content_type varchar(127),
    max_bytes bigint,
    eager_transformation_names varchar(50)[],
    incoming_transformation_name varchar(50),
    visibility varchar(20) not null,
    expires_at timestamptz not null,
    max_uses int not null,
    uses int not null default 0,
    status varchar(20) not null,
    created_at timestamptz not null default now()
);

create index upload_permission_token_hash on upload_permission (token_hash);
create index upload_permission_upload_key on upload_permission (upload_key);
```

```sql
-- 001-add-visibility-and-permission-links.sql

alter table upload
    add column visibility varchar(20) not null default 'PUBLIC';

alter table upload_attempt
    add column visibility varchar(20) not null default 'PUBLIC';

alter table upload_attempt
    add column upload_permission_id uuid references upload_permission(id);
```

`version` is derived from `upload.created_at`; no column is needed.

## 6. Implementation steps

Each step is independently executable and verifiable.

### Phase 0 — Baseline

- [ ] **0.1 Confirm the baseline**
  - Stop the development stack if it is running:
    ```bash
    cd java/modules/media/deployment
    ./run.bash down
    ```
  - Run the current unit and integration tests:
    ```bash
    cd java
    ./gradlew :media-contract:test :media-registry:test :media-api:test :media-client:test
    ./gradlew :media-api:integrationTest
    ```
  - Record any pre-existing failures before changing code.

### Phase 1 — Contract and schema foundations

- [ ] **1.1 Add the visibility vocabulary**
  - Files:
    - `media-contract/src/main/java/com/example/media/contract/upload/UploadVisibility.java`
    - `media-contract/src/main/java/com/example/media/contract/upload/Upload.java`
    - `media-contract/src/main/java/com/example/media/contract/upload/NewUpload.java`
    - `media-contract/src/main/java/com/example/media/contract/upload/UploadAttempt.java`
    - `media-contract/src/main/java/com/example/media/contract/upload/NewUploadAttempt.java`
  - Changes:
    - Add `enum UploadVisibility { PUBLIC, PRIVATE }`.
    - Add `UploadVisibility visibility` to the four records.
    - Update constructors and `Objects.requireNonNull`.
  - Verify:
    ```bash
    ./gradlew :media-contract:test
    ```

- [ ] **1.2 Add upload permission contract types**
  - Files:
    - `media-contract/.../upload/UploadPermissionId.java`
    - `media-contract/.../upload/UploadPermissionStatus.java`
    - `media-contract/.../upload/UploadPermission.java`
    - `media-contract/.../upload/NewUploadPermission.java`
    - `media-contract/.../port/UploadPermissionRegistry.java`
    - `media-contract/.../upload/UploadPermissionDeniedException.java`
    - `media-contract/.../upload/UploadPermissionExpiredException.java`
    - `media-contract/.../upload/UploadPermissionExhaustedException.java`
  - Changes:
    - Define the permission record with id, key, constraints, visibility, expiry, uses, status, and token hash.
    - Define the registry port:
      - `UploadPermission create(NewUploadPermission permission, String tokenHash)`
      - `Optional<UploadPermission> findByTokenHash(String tokenHash)`
      - `UploadPermission consume(String tokenHash, Instant now)`
      - `void revokeForPermission(UploadPermissionId permissionId)`
      - `void revokeForAttempt(AttemptId attemptId)`
  - Verify:
    ```bash
    ./gradlew :media-contract:test
    ```

- [ ] **1.3 Add the database schema**
  - Files:
    - `media-registry/src/main/resources/db/changelog/permissions/000-create-upload-permission-table.sql`
    - `media-registry/src/main/resources/db/changelog/permissions/001-add-visibility-and-permission-links.sql`
    - `media-registry/src/main/resources/db/db.changelog-master.yaml`
  - Changes:
    - Add the two SQL files from section 5.
    - Include `changelog/permissions` after `changelog/upload_process`.
  - Verify:
    ```bash
    ./gradlew :media-registry:test
    ```
    The integration test starts CockroachDB and runs the migrations.

- [ ] **1.4 Add the registry adapter**
  - Files:
    - `media-registry/.../upload/UploadPermissionEntity.java`
    - `media-registry/.../upload/UploadPermissionCrudRepository.java`
    - `media-registry/.../upload/JpaUploadPermissionRegistry.java`
    - `media-registry/.../upload/UploadEntity.java`
    - `media-registry/.../upload/UploadAttemptEntity.java`
    - `media-registry/.../upload/JpaUploadRegistry.java`
  - Changes:
    - Map the new table.
    - Implement `consume` with a pessimistic write lock and the expiry/uses/status checks.
    - Persist `visibility` and `upload_permission_id` when creating attempts and uploads.
  - Verify:
    ```bash
    ./gradlew :media-registry:test
    ```

- [ ] **1.5 Update application services for the new fields**
  - Files:
    - `media-api/.../upload/StartImageUpload.java`
    - `media-api/.../upload/CompleteImageUpload.java`
    - `media-api/.../upload/FailImageUpload.java`
  - Changes:
    - Pass `UploadVisibility.PUBLIC` for now.
    - Keep the existing behavior unchanged until the permission feature is enabled.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **1.6 Add contract and registry tests**
  - Files:
    - `media-contract/src/test/...`
    - `media-registry/src/test/.../JpaUploadPermissionIT.java`
    - Update `JpaUploadRegistryIT.java`
  - Tests:
    - create/read permission,
    - consume increments uses,
    - expired permission fails,
    - exhausted permission fails,
    - two concurrent consumes with `maxUses = 1`; exactly one succeeds.
  - Verify:
    ```bash
    ./gradlew :media-contract:test :media-registry:test
    ```

### Phase 2 — Upload permission issuance

- [ ] **2.1 Add `UploadPermissionService`**
  - Files:
    - `media-api/.../security/UploadPermissionService.java`
    - `media-api/.../security/UploadPermissionProperties.java` or `@Value` configuration
  - Changes:
    - Generate `up1_` + 32 random bytes.
    - Hash with SHA-256.
    - Clamp TTL and `maxUses` to configured maximums.
    - Generate a URL-safe upload key when the request omits one.
    - Call `UploadPermissionRegistry.create`.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **2.2 Add the admin endpoint**
  - Files:
    - `media-api/.../web/AdminUploadPermissionController.java`
    - `media-api/.../web/AdminUploadPermissionRequest.java`
    - `media-api/.../web/AdminUploadPermissionResponse.java`
    - `media-api/.../web/ApiExceptionHandler.java`
  - Changes:
    - `POST /api/admin/upload-permission`.
    - Set `Cache-Control: no-store`.
    - Map bad input to 400.
    - Map missing/wrong admin key to 401 through the existing filter.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```
    Add MockMvc tests for the response shape and headers.

- [ ] **2.3 Add the admin client method**
  - Files:
    - `media-client/.../MediaAdminClient.java`
    - `media-client/src/test/.../MediaAdminClientTest.java`
  - Changes:
    - Add request/response records.
    - Add `createUploadPermission`.
    - Test the request shape and `X-Admin-Key`.
  - Verify:
    ```bash
    ./gradlew :media-client:test
    ```

### Phase 3 — Upload enforcement

- [ ] **3.1 Add request plumbing and configuration**
  - Files:
    - `media-api/.../web/ImageUploadRequest.java`
    - `media-api/.../web/ImageUploadController.java`
    - `media-api/.../upload/StartImageUpload.java`
    - `media-api/src/main/resources/application.yaml`
  - Changes:
    - Add `permission` to the JSON body and request record.
    - Add `media.upload-permission.required`, `default-ttl`, `max-ttl`, `default-max-uses`, `max-uses`.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **3.2 Consume and validate the permission**
  - Files:
    - `media-api/.../upload/StartImageUpload.java`
    - `media-api/.../security/UploadPermissionValidator.java`
  - Changes:
    - Inside the existing transaction, consume the permission and validate every constraint.
    - Use the permission’s upload key and visibility.
    - Record `upload_permission_id` on the attempt.
    - Reject mismatches with 403.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **3.3 Map permission failures**
  - Files:
    - `media-api/.../web/ApiExceptionHandler.java`
  - Changes:
    - Map `UploadPermissionDeniedException`, expired, and exhausted to 403.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **3.4 Revoke on terminal failure**
  - Files:
    - `media-api/.../upload/FailImageUpload.java`
    - `media-api/.../upload/CompleteImageUpload.java`
  - Changes:
    - On `FAILED`, revoke the permission linked to the attempt.
    - On `SUCCESS`, leave the permission in its consumed/exhausted state.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **3.5 Update the Java client and end-to-end uploads**
  - Files:
    - `media-client/.../MediaClient.java`
    - `media-client/src/test/.../MediaClientTest.java`
    - `media-api/src/integrationTest/.../EndToEndTest.java`
  - Changes:
    - Add `permission` to `UploadImageRequest`.
    - In the e2e test, mint a permission through `MediaAdminClient` before every upload.
    - Add cases: missing, expired, exhausted, wrong key, mismatched constraints.
  - Verify:
    ```bash
    ./gradlew :media-client:test
    ./gradlew :media-api:integrationTest
    ```

### Phase 4 — Private reads, download tokens, and cache versions

- [ ] **4.1 Add `DownloadTokenService`**
  - Files:
    - `media-api/.../security/DownloadTokenService.java`
    - `media-api/.../security/DownloadTokenProperties.java`
    - `media-api/src/test/.../DownloadTokenServiceTest.java`
  - Changes:
    - Build the canonical string.
    - Sign with HMAC-SHA256.
    - Base64url-encode the signature.
    - Verify with `MessageDigest.isEqual`.
    - Require at least 32 bytes of secret at startup.
    - Support key id and rotation.
    - Add fixed test vectors.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **4.2 Add the admin download endpoint**
  - Files:
    - `media-api/.../web/AdminDownloadPermissionController.java`
    - `media-api/.../web/AdminDownloadPermissionRequest.java`
    - `media-api/.../web/AdminDownloadPermissionResponse.java`
    - `media-client/.../MediaAdminClient.java`
  - Changes:
    - `POST /api/admin/download-permission`.
    - Load the finalized upload and compute its version.
    - Return `uploadKey`, `version`, `expires`, `signature`, `keyId`, `pathPrefix`.
    - Set `Cache-Control: no-store`.
  - Verify:
    ```bash
    ./gradlew :media-api:test :media-client:test
    ```

- [ ] **4.3 Expose the version in responses and read URLs**
  - Files:
    - `media-api/.../web/ImageUploadResponse.java`
    - `media-api/.../web/UploadStatusResponse.java`
    - `media-api/.../upload/ReadUploadStatus.java`
    - `media-api/.../web/UploadStatusController.java`
    - `media-client/.../MediaClient.java`
    - `typescript/media-client/src/types.ts`
  - Changes:
    - Compute `version = Long.toString(upload.createdAt().toEpochMilli())`.
    - Blocking upload response: `version` non-null.
    - Async accepted response: `version` null.
    - Status response: `version` non-null when `SUCCESS`.
    - Clients mirror the field.
  - Verify:
    ```bash
    ./gradlew :media-api:test :media-client:test
    cd ../typescript/media-client && npm test
    ```

- [ ] **4.4 Enforce visibility and version on reads**
  - Files:
    - `media-api/.../read/ReadOriginal.java`
    - `media-api/.../read/ReadVariant.java`
    - `media-api/.../read/StoredFile.java`
    - `media-api/.../web/ImageReadController.java`
    - `media-api/.../security/DownloadCredential.java`
  - Changes:
    - Add `v`, `e`, `s`, `k` query parameters.
    - For `PUBLIC`, serve normally and ignore the credential.
    - For `PRIVATE`, require a valid token and a matching version.
    - Add the upload visibility to `StoredFile` so the controller can choose the cache header.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **4.5 Set cache headers**
  - Files:
    - `media-api/.../web/ImageReadController.java`
    - `media-api/src/main/resources/application.yaml`
  - Changes:
    - Public read: `public, max-age=31536000, immutable` by default.
    - Private read: `private, max-age=300` by default.
    - No `no-store` on media.
    - Permission/token responses remain `no-store`.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **4.6 Update clients and e2e for private reads**
  - Files:
    - `media-client/.../MediaClient.java`
    - `media-client/src/test/.../MediaClientTest.java`
    - `typescript/media-client/src/client.ts`
    - `typescript/media-client/test/client.test.ts`
    - `typescript/media-client/e2e/smoke.test.ts`
    - `media-api/src/integrationTest/.../EndToEndTest.java`
  - Changes:
    - Add overloads that accept a version and download credential.
    - Test private reads with and without a token.
    - Test an expired token.
    - Test a token for another key.
    - Test that one token reads the original and all variants.
    - Test that a new version invalidates the old token.
  - Verify:
    ```bash
    ./gradlew :media-client:test
    ./gradlew :media-api:integrationTest
    cd ../typescript/media-client && npm test
    ```

### Phase 5 — Minimal nginx edge

- [ ] **5.1 Add the nginx service**
  - Files:
    - `media/deployment/nginx.conf`
    - `media/deployment/download_token.js`
    - `media/deployment/nginx.Dockerfile` or a suitable OpenResty/nginx+njs image
    - `media/deployment/compose.yaml`
  - Changes:
    - Add a `cdn` service on a fixed host port, for example `8083`.
    - Implement `/cdn/` with njs `js_access`.
    - Extract the upload key from `/cdn/{uploadKey}...`.
    - Require `v`, `e`, `s`, and `k` when a token is present.
    - Recompute the HMAC and compare in constant time.
    - Proxy to the media API read endpoints.
    - Pass `v` and the auth query parameters through unchanged.
  - Verify:
    ```bash
    cd java/modules/media/deployment
    ./run.bash up -d
    ./run.bash logs -f cdn
    ```

- [ ] **5.2 Share the download secret**
  - Files:
    - `media/deployment/compose.yaml`
    - `media-api/src/main/resources/application.yaml`
    - `media-api/src/integrationTest/.../EndToEndTest.java`
    - `media-api/src/integrationTest/.../MediaComposeStack.java`
  - Changes:
    - Add `MEDIA_DOWNLOAD_TOKEN_SECRET` and `MEDIA_DOWNLOAD_TOKEN_KEY_ID`.
    - Use the same values in the API and nginx.
    - In the e2e test, set the API property and pass the same values to the compose service.
  - Verify:
    ```bash
    ./gradlew :media-api:integrationTest
    ```

- [ ] **5.3 Update the compose test stack**
  - Files:
    - `media-api/src/integrationTest/.../MediaComposeStack.java`
  - Changes:
    - Wait for the CDN port.
    - Add the CDN port to the “stack already running” port check.
  - Verify:
    ```bash
    ./gradlew :media-api:integrationTest
    ```

- [ ] **5.4 Add CDN end-to-end journeys**
  - Files:
    - `media-api/src/integrationTest/.../EndToEndTest.java`
  - Cases:
    - Public read through `/cdn/` without a token.
    - Private read through `/cdn/` with a valid token.
    - Missing token → 403.
    - Expired token → 403.
    - Tampered signature → 403.
    - Token for key A cannot read key B.
    - Token for an old version cannot read a replaced version.
  - Verify:
    ```bash
    ./gradlew :media-api:integrationTest
    ```

- [ ] **5.5 Document the token contract**
  - Files:
    - `modules/media/deployment/AGENTS.md`
    - `modules/media/AGENTS.md`
  - Changes:
    - Document the canonical string, URL shape, version field, cache headers, secret variables, and log-redaction rule.
    - Document that nginx is the edge verifier and that the API remains authoritative.
  - Verify:
    - Review the docs against the implemented test vectors.

### Phase 6 — Hardening and optional full CDN

- [ ] **6.1 Harden the admin boundary**
  - Files:
    - `media-api/.../web/AdminKeyFilter.java`
  - Changes:
    - Use a constant-time comparison.
    - Reject an empty configured key at startup.
  - Verify:
    ```bash
    ./gradlew :media-api:test
    ```

- [ ] **6.2 Add rotation and operational safeguards**
  - Files:
    - `DownloadTokenService`, configuration, nginx config.
  - Changes:
    - Support `kid`-based rotation with overlapping old/new secrets.
    - Add clock-skew tolerance.
    - Mask `e`/`s`/`k` in access logs.
    - Set `Referrer-Policy: no-referrer` on media responses.
  - Verify:
    - Unit tests for old and new key ids.

- [ ] **6.3 Optional: enable edge caching**
  - Files:
    - `media/deployment/nginx.conf`
    - `media-api/.../web/` internal authorization endpoint
  - Changes:
    - Add `auth_request` before `proxy_cache`.
    - Authorization returns 204 for public uploads and for valid private tokens, 403 otherwise.
    - Cache key includes `$uri$arg_v`, excludes `e`/`s`/`k`.
    - Ensure a request without authorization never reaches the cache.
  - Verify:
    - Add a test that a cached private response is not served without a token.

- [ ] **6.4 Defer the direct-Minio CDN**
  - Document the remaining design work:
    - `uploadKey` → asset prefix/variant resolution,
    - direct Minio credentials or `auth_request` resolver,
    - private cache isolation,
    - regional routing and replication.
  - Do not implement this in the authorization slice.

## 7. Test matrix

### Upload permission

- [ ] Valid permission → upload succeeds.
- [ ] Missing permission → 403.
- [ ] Unknown token → 403.
- [ ] Expired permission → 403.
- [ ] Exhausted `maxUses` → 403 on the next attempt.
- [ ] Permission for key A cannot upload key B.
- [ ] Extension mismatch → 403.
- [ ] Content-type mismatch → 403.
- [ ] File-size mismatch → 403.
- [ ] Eager-transformation order mismatch → 403.
- [ ] Incoming-transformation mismatch → 403.
- [ ] Terminal failed attempt revokes remaining uses.
- [ ] Rolled-back attempt transaction does not consume a use.
- [ ] Two concurrent consumes with `maxUses = 1`: exactly one succeeds.

### Download permission and versioning

- [ ] Private upload without a token → 403.
- [ ] Private upload with a valid token → 200 for the original and every variant.
- [ ] Expired token → 403.
- [ ] Tampered signature → 403.
- [ ] Token for key A cannot read key B.
- [ ] Token for version A cannot read version B.
- [ ] Public upload without a token → 200.
- [ ] Direct API access with an invalid token → 403.
- [ ] `Cache-Control` is absent of `no-store` for media responses.
- [ ] Token/permission responses contain `Cache-Control: no-store`.
- [ ] Blocking upload exposes the version.
- [ ] Async status exposes the version only after `SUCCESS`.

### Nginx

- [ ] Valid token passes.
- [ ] Missing/invalid/expired/tampered token → 403.
- [ ] Prefix scope works for original and variants.
- [ ] Version is part of the signature.
- [ ] Public reads pass without a token.
- [ ] Private reads without a token are rejected by the API even if they pass the edge.
- [ ] Auth runs before cache when edge caching is enabled.

## 8. Configuration

```yaml
media:
  upload-permission:
    required: true
    default-ttl: 10m
    max-ttl: 1h
    default-max-uses: 1
    max-uses: 10
  download-token:
    secret: ${MEDIA_DOWNLOAD_TOKEN_SECRET}
    key-id: ${MEDIA_DOWNLOAD_TOKEN_KEY_ID:k1}
    ttl: 15m
    max-ttl: 1h
    clock-skew: 5s
  read:
    public-cache-control: "public, max-age=31536000, immutable"
    private-cache-control: "private, max-age=300"
  cdn:
    base-url: ${MEDIA_CDN_BASE_URL:http://localhost:8083}
```

- The download secret must be at least 32 bytes after decoding.
- Development and end-to-end values are local defaults; production must inject secrets through the environment.
- The admin key remains `MEDIA_ADMIN_KEY`.
- Set `media.upload-permission.required=true` in the end-to-end test from Phase 3 onward.

## 9. Risks and limitations

- Upload and download tokens are bearer credentials. TLS and log redaction are mandatory.
- The static admin key is the trust anchor until signed admin requests or mTLS land. Keep it server-side only.
- Download tokens cannot be revoked before expiry without an additional `auth_request`/revocation check.
- Private media is cacheable. Any edge cache must authorize before cache lookup; otherwise a cached private response could leak.
- The version is derived from `upload.createdAt`. A future “replace upload” feature must create a new upload row with a new `createdAt`, not mutate the old row in place.
- Tightening `UploadKey` to `[A-Za-z0-9_-]{1,100}` can break existing rows with other characters. Handle this with a data check or validate only on new keys if existing data requires it.
- Nginx `secure_link` is MD5-only and cannot verify RSA. HMAC-SHA256 via njs or OpenResty is the recommended verifier.
- Direct Minio CDN access is deliberately deferred. It requires an upload-key resolver and a safe private-cache design.

## 10. Open decisions

These are the remaining choices to confirm during implementation:

1. Default `maxUses`: `1` (strict) or `3` (retry-tolerant).
2. Revoke remaining uses on terminal `FAILED`: yes (recommended) or no.
3. Private cache header: `private, max-age=300` (browser cache) or `public, max-age=...` (controlled edge cache).
4. nginx implementation: njs HMAC (recommended) or OpenResty Lua.
5. Upload key generation: server-generated by default, or always supplied by the main application.
6. Whether the admin may upload without a permission in a future admin-only endpoint.
