# File upload and import security — Item 3

The current mobile screens download authenticated JSON snapshots; they do not expose a public file-upload interface. The server retains a scoped JSON production-write endpoint. The administrator publishes snapshot files locally using `server/publish.mjs`.

## Safeguards

- Snapshot publication now reads a regular file with a strict 2 MiB bound **before and during** reading, then validates the existing estate schema before changing the published snapshot.
- Existing snapshot validation checks strict UTF-8, estate ID, schema version, revision, seven sections, row and cell limits.
- Production POST requires a bearer token with `production:write` scope, JSON content type, request size limit, and record validation.
- New tests cover oversized and malformed snapshot input, incorrect estate identity, non-file input, unauthorized writes, wrong content type, malformed JSON, oversized POST bodies and unchanged production revision.

## Limitations

- These changes do not add ZIP, XLSX, image or arbitrary file uploads; future import formats require their own validation, decompression limits and path traversal defenses.
- Do not infer file safety from extensions or MIME types alone.
- File publisher still runs on the administrator's trusted server. It must not be exposed as a public upload endpoint.
- Test execution and deployment must be verified in CI before merging; this PR does not change the live VPS.
