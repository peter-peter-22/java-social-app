# @social-app/media-client

The TypeScript browser client of the media service web API: uploads images, reads originals and
variants, and polls the queued upload status. It mirrors the Java `media-client` module and speaks
the shared media JSON shape (the wire types in `src/types.ts` are the `media-json` shapes).

## Commands

- `npm install` — install the dev toolchain (TypeScript; tests run on the built-in `node:test`).
- `npm test` — unit tests against a fake `fetch`.
- `npm run test:e2e` — the smoke journey (upload → read → status) against a running stack. Skipped
  when no API is reachable at `MEDIA_API_URL` (default `http://localhost:8090`).

## Running the smoke journey

Start the compose stack (`../java/modules/media/deployment/run.bash up -d`) and the media API with
`MINIO_RECONCILIATION_ENABLED=true` (so it provisions the uploads bucket), or leave the stack up
after `./gradlew :media-api:integrationTest` in `../java`. The admin key comes from
`MEDIA_ADMIN_KEY` (default `dev-admin-key`).