# Media client

The client SDK of the media service: two Spring `RestClient` facades over the web API, speaking the
shared media JSON shape (`media-json`).

- `MediaAdminClient` — the admin facade: registers named transformations and issues upload
  permissions with the `X-Admin-Key` header (admin uploads come later).
- `MediaClient` — the browser's operations: multipart image upload (presenting the issued upload
  permission), read original and variant, poll the queued upload status.

Depends on the contract and `media-json` only; the `ArchitectureTest` guards it away from the
server internals. Error answers throw `RestClientResponseException` (standard RestClient
behavior). The TypeScript browser client in `../../typescript/media-client` mirrors this module.

## Tests

`MockRestServiceServer`-based: the request shapes and the response parsing are locked per facade.